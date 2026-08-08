package com.listaih.app.data.repository

import com.listaih.app.data.local.AppDatabase
import com.listaih.app.data.local.dao.ListItemDao
import com.listaih.app.data.local.dao.ShoppingListDao
import com.listaih.app.data.local.dao.SyncQueueDao
import com.listaih.app.data.local.entity.ListItemEntity
import com.listaih.app.data.local.entity.ShoppingListEntity
import com.listaih.app.data.local.entity.SyncQueueEntity
import com.listaih.app.data.network.ApiService
import com.listaih.app.data.network.model.*
import com.listaih.app.data.preferences.AppPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class ShoppingRepository @Inject constructor(
    private val database: AppDatabase,
    private val apiService: ApiService,
    private val appPreferences: AppPreferences
) {

    private val shoppingListDao: ShoppingListDao = database.shoppingListDao()
    private val listItemDao: ListItemDao = database.listItemDao()
    private val syncQueueDao: SyncQueueDao = database.syncQueueDao()

    fun getActiveLists(householdId: String): Flow<List<ShoppingListEntity>> {
        return shoppingListDao.getActiveLists(householdId)
    }

    fun getArchivedLists(householdId: String): Flow<List<ShoppingListEntity>> {
        return shoppingListDao.getArchivedLists(householdId)
    }

    fun getListItems(listId: String): Flow<List<ListItemEntity>> {
        return listItemDao.getItemsByListId(listId)
    }

    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val response = apiService.login(LoginRequest(email, password)).await()
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    appPreferences.setAccessToken(body.accessToken).await()
                    appPreferences.setRefreshToken(body.refreshToken).await()
                    appPreferences.setUserId(body.user.id).await()
                    appPreferences.setHouseholdId("").await() // Will be set after household selection
                    Result.success(body)
                } ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun refreshAccessToken(): Boolean {
        return try {
            val refreshToken = appPreferences.getRefreshToken().blockingFirst() ?: return false
            val response = apiService.refreshToken("Bearer $refreshToken").await()
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    appPreferences.setAccessToken(body.accessToken).await()
                    appPreferences.setRefreshToken(body.refreshToken).await()
                    true
                } ?: false
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun logout() {
        val accessToken = appPreferences.getAccessToken().blockingFirst()
        accessToken?.let { token ->
            apiService.logout("Bearer $token").await()
        }
        appPreferences.clearAuth().await()
    }

    suspend fun syncLists(householdId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val accessToken = appPreferences.getAccessToken().blockingFirst() ?: return@withContext Result.failure(Exception("No access token"))
                val response = apiService.getLists("Bearer $accessToken", householdId).await()
                if (response.isSuccessful) {
                    val lists = response.body() ?: emptyList()
                    val entities = lists.map { toEntity(it) }
                    shoppingListDao.insertAll(entities)
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(response.errorBody()?.string() ?: "Sync failed"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun syncListItems(listId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val accessToken = appPreferences.getAccessToken().blockingFirst() ?: return@withContext Result.failure(Exception("No access token"))
                val response = apiService.getList("Bearer $accessToken", listId).await()
                if (response.isSuccessful) {
                    val list = response.body() ?: return@withContext Result.failure(Exception("Empty list"))
                    val entities = list.items.map { toEntity(it) }
                    listItemDao.insertAll(entities)
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(response.errorBody()?.string() ?: "Sync failed"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun createList(householdId: String, name: String, category: String?): Result<ShoppingListEntity> {
        return withContext(Dispatchers.IO) {
            try {
                val accessToken = appPreferences.getAccessToken().blockingFirst() ?: return@withContext Result.failure(Exception("No access token"))
                val response = apiService.createList("Bearer $accessToken", householdId, CreateListRequest(name, category)).await()
                if (response.isSuccessful) {
                    val list = response.body() ?: return@withContext Result.failure(Exception("Empty response"))
                    val entity = toEntity(list)
                    shoppingListDao.insert(entity)
                    Result.success(entity)
                } else {
                    Result.failure(Exception(response.errorBody()?.string() ?: "Create failed"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun updateList(listId: String, name: String?, category: String?, archivedAt: Long?): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val accessToken = appPreferences.getAccessToken().blockingFirst() ?: return@withContext Result.failure(Exception("No access token"))
                val request = UpdateListRequest(
                    name = name,
                    category = category,
                    archivedAt = archivedAt?.let { java.time.Instant.ofEpochMilli(it).toString() }
                )
                val response = apiService.updateList("Bearer $accessToken", listId, request).await()
                if (response.isSuccessful) {
                    // Update local
                    shoppingListDao.getListById(listId).first().also { existing ->
                        existing?.let { entity ->
                            name?.let { entity.name = it }
                            category?.let { entity.category = it }
                            archivedAt?.let { entity.archivedAt = it }
                            entity.updatedAt = System.currentTimeMillis()
                            entity.serverSynced = true
                            shoppingListDao.update(entity)
                        }
                    }
                    Result.success(Unit)
                } else {
                    // Queue for later sync
                    queueSync("list", listId, "update", request)
                    Result.failure(Exception(response.errorBody()?.string() ?: "Update failed"))
                }
            } catch (e: Exception) {
                queueSync("list", listId, "update", UpdateListRequest(name, category, archivedAt?.let { java.time.Instant.ofEpochMilli(it).toString() }))
                Result.failure(e)
            }
        }
    }

    suspend fun deleteList(listId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val accessToken = appPreferences.getAccessToken().blockingFirst() ?: return@withContext Result.failure(Exception("No access token"))
                val response = apiService.deleteList("Bearer $accessToken", listId).await()
                if (response.isSuccessful) {
                    shoppingListDao.getListById(listId).first()?.let { shoppingListDao.delete(it) }
                    Result.success(Unit)
                } else {
                    queueSync("list", listId, "delete", mapOf())
                    Result.failure(Exception(response.errorBody()?.string() ?: "Delete failed"))
                }
            } catch (e: Exception) {
                queueSync("list", listId, "delete", mapOf())
                Result.failure(e)
            }
        }
    }

    suspend fun createItem(listId: String, name: String, quantity: Double, unit: String, estimatedPrice: Double?, category: String?): Result<ListItemEntity> {
        return withContext(Dispatchers.IO) {
            try {
                val accessToken = appPreferences.getAccessToken().blockingFirst() ?: return@withContext Result.failure(Exception("No access token"))
                val response = apiService.createItem("Bearer $accessToken", listId, CreateItemRequest(name, quantity, unit, estimatedPrice, category)).await()
                if (response.isSuccessful) {
                    val item = response.body() ?: return@withContext Result.failure(Exception("Empty response"))
                    val entity = toEntity(item)
                    listItemDao.insert(entity)
                    Result.success(entity)
                } else {
                    Result.failure(Exception(response.errorBody()?.string() ?: "Create failed"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun updateItem(itemId: String, listId: String, request: UpdateItemRequest): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val accessToken = appPreferences.getAccessToken().blockingFirst() ?: return@withContext Result.failure(Exception("No access token"))
                val response = apiService.updateItem("Bearer $accessToken", listId, itemId, request).await()
                if (response.isSuccessful) {
                    // Handle checked status update with actualPrice calculation
                    if (request.checked == true) {
                        listItemDao.getItemById(itemId).first()?.let { existing ->
                            val updated = existing.copy(
                                checked = true,
                                checkedBy = "current_user", // TODO: get from preferences
                                checkedAt = System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis(),
                                actualPrice = request.estimatedPrice?.let { it * existing.quantity } ?: existing.actualPrice,
                                serverSynced = true
                            )
                            listItemDao.update(updated)
                        }
                    } else {
                        listItemDao.getItemById(itemId).first()?.let { existing ->
                            val updated = existing.copy(
                                name = request.name ?: existing.name,
                                quantity = request.quantity ?: existing.quantity,
                                unit = request.unit ?: existing.unit,
                                estimatedPrice = request.estimatedPrice ?: existing.estimatedPrice,
                                category = request.category ?: existing.category,
                                checked = request.checked ?: existing.checked,
                                position = request.position ?: existing.position,
                                updatedAt = System.currentTimeMillis(),
                                serverSynced = true
                            )
                            listItemDao.update(updated)
                        }
                    }
                    Result.success(Unit)
                } else {
                    queueSync("item", itemId, "update", request)
                    Result.failure(Exception(response.errorBody()?.string() ?: "Update failed"))
                }
            } catch (e: Exception) {
                queueSync("item", itemId, "update", request)
                Result.failure(e)
            }
        }
    }

    suspend fun deleteItem(itemId: String, listId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val accessToken = appPreferences.getAccessToken().blockingFirst() ?: return@withContext Result.failure(Exception("No access token"))
                val response = apiService.deleteItem("Bearer $accessToken", listId, itemId).await()
                if (response.isSuccessful) {
                    listItemDao.getItemById(itemId).first()?.let { listItemDao.delete(it) }
                    Result.success(Unit)
                } else {
                    queueSync("item", itemId, "delete", mapOf())
                    Result.failure(Exception(response.errorBody()?.string() ?: "Delete failed"))
                }
            } catch (e: Exception) {
                queueSync("item", itemId, "delete", mapOf())
                Result.failure(e)
            }
        }
    }

    suspend fun getHealth(): Result<HealthResponse> {
        return try {
            val response = apiService.getHealth().await()
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Health check failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSystemConfig(): Result<SystemConfigResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val accessToken = appPreferences.getAccessToken().blockingFirst() ?: return@withContext Result.failure(Exception("No access token"))
                val response = apiService.getSystemConfig("Bearer $accessToken").await()
                if (response.isSuccessful) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception(response.errorBody()?.string() ?: "Config fetch failed"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getPurchases(householdId: String): Result<List<PurchaseResponse>> {
        return withContext(Dispatchers.IO) {
            try {
                val accessToken = appPreferences.getAccessToken().blockingFirst() ?: return@withContext Result.failure(Exception("No access token"))
                val response = apiService.getPurchases("Bearer $accessToken", householdId).await()
                if (response.isSuccessful) {
                    Result.success(response.body() ?: emptyList())
                } else {
                    Result.failure(Exception(response.errorBody()?.string() ?: "Failed to fetch purchases"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun checkout(listId: String, request: CheckoutRequest): Result<PurchaseResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val accessToken = appPreferences.getAccessToken().blockingFirst() ?: return@withContext Result.failure(Exception("No access token"))
                val response = apiService.checkout("Bearer $accessToken", listId, request).await()
                if (response.isSuccessful) {
                    Result.success(response.body()!!)
                } else {
                    val errBody = response.errorBody()?.string()
                    if (errBody != null) {
                        val err = json.decodeFromString<ApiError>(errBody)
                        Result.failure(Exception(err.message ?: "Checkout failed"))
                    } else {
                        Result.failure(Exception("Checkout failed"))
                    }
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun updatePurchase(purchaseId: String, request: UpdatePurchaseRequest): Result<PurchaseResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val accessToken = appPreferences.getAccessToken().blockingFirst() ?: return@withContext Result.failure(Exception("No access token"))
                val response = apiService.updatePurchase("Bearer $accessToken", purchaseId, request).await()
                if (response.isSuccessful) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception(response.errorBody()?.string() ?: "Update failed"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun updateSystemConfig(config: Map<String, Any>): Result<SystemConfigResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val accessToken = appPreferences.getAccessToken().blockingFirst() ?: return@withContext Result.failure(Exception("No access token"))
                val response = apiService.updateSystemConfig("Bearer $accessToken", config).await()
                if (response.isSuccessful) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception(response.errorBody()?.string() ?: "Config update failed"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun queueSync(entityType: String, entityId: String, operation: String, payload: Any) {
        val queueItem = SyncQueueEntity(
            entityType = entityType,
            entityId = entityId,
            operation = operation,
            payload = kotlinx.serialization.json.Json.encodeToString(payload as kotlinx.serialization.KSerializer<*>),
            createdAt = System.currentTimeMillis()
        )
        syncQueueDao.insert(queueItem)
    }

    private fun toEntity(list: ShoppingListResponse): ShoppingListEntity {
        return ShoppingListEntity(
            id = list.id,
            name = list.name,
            category = list.category,
            householdId = list.householdId,
            archivedAt = list.archivedAt?.let { java.time.Instant.parse(it).toEpochMilli() },
            createdAt = java.time.Instant.parse(list.createdAt).toEpochMilli(),
            updatedAt = java.time.Instant.parse(list.updatedAt).toEpochMilli(),
            serverSynced = true
        )
    }

    private fun toEntity(item: ListItemResponse): ListItemEntity {
        return ListItemEntity(
            id = item.id,
            listId = item.listId,
            name = item.name,
            quantity = item.quantity,
            unit = item.unit,
            estimatedPrice = item.estimatedPrice,
            actualPrice = item.actualPrice,
            category = item.category,
            checked = item.checked,
            checkedBy = item.checkedBy,
            checkedAt = item.checkedAt?.let { java.time.Instant.parse(it).toEpochMilli() },
            position = item.position,
            createdAt = java.time.Instant.parse(item.createdAt).toEpochMilli(),
            updatedAt = java.time.Instant.parse(item.updatedAt).toEpochMilli(),
            serverSynced = true
        )
    }
}