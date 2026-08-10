package com.listaih.app.data.repository

import com.listaih.app.data.local.AppDatabase
import com.listaih.app.data.local.dao.ListItemDao
import com.listaih.app.data.local.dao.ShoppingListDao
import com.listaih.app.data.local.dao.SyncQueueDao
import com.listaih.app.data.local.entity.ListItemEntity
import com.listaih.app.data.local.entity.ShoppingListEntity
import com.listaih.app.data.local.entity.ShoppingListWithCounts
import com.listaih.app.data.local.entity.SyncQueueEntity
import com.listaih.app.data.network.ApiService
import com.listaih.app.data.network.model.*
import com.listaih.app.data.preferences.AppPreferences
import com.listaih.app.ui.screens.home.ShoppingListUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class ExportPayload(
    val lists: List<ShoppingListEntity>,
    val items: List<ListItemEntity>
)

@Singleton
class ShoppingRepository @Inject constructor(
    private val database: AppDatabase,
    private val apiService: ApiService,
    private val appPreferences: AppPreferences
) {

    private val json = Json

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

    fun getActiveListsUiFlow(): Flow<List<ShoppingListUi>> {
        val householdId = appPreferences.getHouseholdId() ?: return flowOf(emptyList())
        if (householdId.isBlank()) return flowOf(emptyList())
        return shoppingListDao.getActiveListsWithCounts(householdId).map { rows ->
            rows.map { it.toUi() }
        }
    }

    suspend fun getHouseholds(): Result<List<HouseholdResponse>> {
        return withContext(Dispatchers.IO) {
            try {
                val accessToken = appPreferences.getAccessToken() ?: return@withContext Result.failure(Exception("No access token"))
                val response = apiService.getHouseholds("Bearer $accessToken")
                if (response.isSuccessful) {
                    Result.success(response.body() ?: emptyList())
                } else {
                    Result.failure(Exception(response.errorBody()?.string() ?: "Failed to fetch households"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    fun saveHouseholdId(householdId: String) {
        appPreferences.setHouseholdId(householdId).blockingAwait()
    }

    suspend fun getPurchase(purchaseId: String): Result<PurchaseResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val accessToken = appPreferences.getAccessToken() ?: return@withContext Result.failure(Exception("No access token"))
                val response = apiService.getPurchase("Bearer $accessToken", purchaseId)
                if (response.isSuccessful) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception(response.errorBody()?.string() ?: "Failed to fetch purchase"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getProfile(): Result<UserResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val accessToken = appPreferences.getAccessToken() ?: return@withContext Result.failure(Exception("No access token"))
                val response = apiService.getProfile("Bearer $accessToken")
                if (response.isSuccessful) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception(response.errorBody()?.string() ?: "Failed to fetch profile"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun regenerateInviteCode(householdId: String): Result<HouseholdResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val accessToken = appPreferences.getAccessToken() ?: return@withContext Result.failure(Exception("No access token"))
                val response = apiService.regenerateInviteCode("Bearer $accessToken", householdId)
                if (response.isSuccessful) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception(response.errorBody()?.string() ?: "Failed to regenerate invite code"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun changePassword(current: String, new: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val accessToken = appPreferences.getAccessToken() ?: return@withContext Result.failure(Exception("No access token"))
                val response = apiService.changePassword("Bearer $accessToken", ChangePasswordRequest(current, new))
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(response.errorBody()?.string() ?: "Change password failed"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun exportLocalData(): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val lists = shoppingListDao.getAll()
                val items = listItemDao.getAllItems()
                Result.success(Json.encodeToString(ExportPayload(lists, items)))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    appPreferences.setAccessToken(body.accessToken)
                    appPreferences.setRefreshToken(body.refreshToken)
                    appPreferences.setUserId(body.user.id)
                    appPreferences.setHouseholdId("") // Will be set after household selection
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
            val refreshToken = appPreferences.getRefreshToken() ?: return false
            val response = apiService.refreshToken(RefreshRequest(refreshToken))
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    appPreferences.setAccessToken(body.accessToken)
                    appPreferences.setRefreshToken(body.refreshToken)
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
        val accessToken = appPreferences.getAccessToken()
        accessToken?.let { token ->
            apiService.logout("Bearer $token")
        }
        appPreferences.clearAuth()
    }

    suspend fun syncLists(householdId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val accessToken = appPreferences.getAccessToken() ?: return@withContext Result.failure(Exception("No access token"))
                val response = apiService.getLists("Bearer $accessToken", householdId)
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
                val accessToken = appPreferences.getAccessToken() ?: return@withContext Result.failure(Exception("No access token"))
                val response = apiService.getList("Bearer $accessToken", listId)
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

    suspend fun createList(householdId: String, name: String, category: String?, listType: String? = null): Result<ShoppingListEntity> {
        return withContext(Dispatchers.IO) {
            try {
                val accessToken = appPreferences.getAccessToken() ?: return@withContext Result.failure(Exception("No access token"))
                val response = apiService.createList("Bearer $accessToken", householdId, CreateListRequest(name, category, listType))
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
                val accessToken = appPreferences.getAccessToken() ?: return@withContext Result.failure(Exception("No access token"))
                val request = UpdateListRequest(
                    name = name,
                    category = category,
                    archivedAt = archivedAt?.let { java.time.Instant.ofEpochMilli(it).toString() }
                )
                val response = apiService.updateList("Bearer $accessToken", listId, request)
                if (response.isSuccessful) {
                    shoppingListDao.getListById(listId).first()?.let { entity ->
                        val updated = entity.copy(
                            name = name ?: entity.name,
                            category = category ?: entity.category,
                            archivedAt = archivedAt ?: entity.archivedAt,
                            updatedAt = System.currentTimeMillis(),
                            serverSynced = true
                        )
                        shoppingListDao.update(updated)
                    }
                    Result.success(Unit)
                } else {
                    queueSync("list", listId, "update", Json.encodeToString(request))
                    Result.failure(Exception(response.errorBody()?.string() ?: "Update failed"))
                }
            } catch (e: Exception) {
                queueSync("list", listId, "update", Json.encodeToString(UpdateListRequest(name, category, archivedAt?.let { java.time.Instant.ofEpochMilli(it).toString() })))
                Result.failure(e)
            }
        }
    }

    suspend fun deleteList(listId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val accessToken = appPreferences.getAccessToken() ?: return@withContext Result.failure(Exception("No access token"))
                val response = apiService.deleteList("Bearer $accessToken", listId)
                if (response.isSuccessful) {
                    shoppingListDao.getListById(listId).first()?.let { shoppingListDao.delete(it) }
                    Result.success(Unit)
                } else {
                    queueSync("list", listId, "delete", "{}")
                    Result.failure(Exception(response.errorBody()?.string() ?: "Delete failed"))
                }
            } catch (e: Exception) {
                queueSync("list", listId, "delete", "{}")
                Result.failure(e)
            }
        }
    }

    suspend fun createItem(
        listId: String,
        name: String,
        quantity: Double,
        unit: String,
        estimatedPrice: Double?,
        category: String?,
        barcode: String? = null,
        barcodeRaw: String? = null,
        productId: String? = null
    ): Result<ListItemEntity> {
        return withContext(Dispatchers.IO) {
            try {
                val accessToken = appPreferences.getAccessToken() ?: return@withContext Result.failure(Exception("No access token"))
                val response = apiService.createItem("Bearer $accessToken", listId, CreateItemRequest(name, quantity, unit, estimatedPrice, category, barcode, barcodeRaw, productId))
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
                val accessToken = appPreferences.getAccessToken() ?: return@withContext Result.failure(Exception("No access token"))
                val response = apiService.updateItem("Bearer $accessToken", listId, itemId, request)
                if (response.isSuccessful) {
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
                                barcode = request.barcode ?: existing.barcode,
                                barcodeRaw = request.barcodeRaw ?: existing.barcodeRaw,
                                productId = request.productId ?: existing.productId,
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
                    queueSync("item", itemId, "update", Json.encodeToString(request))
                    Result.failure(Exception(response.errorBody()?.string() ?: "Update failed"))
                }
            } catch (e: Exception) {
                queueSync("item", itemId, "update", Json.encodeToString(request))
                Result.failure(e)
            }
        }
    }

    suspend fun deleteItem(itemId: String, listId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val accessToken = appPreferences.getAccessToken() ?: return@withContext Result.failure(Exception("No access token"))
                val response = apiService.deleteItem("Bearer $accessToken", listId, itemId)
                if (response.isSuccessful) {
                    listItemDao.getItemById(itemId).first()?.let { listItemDao.delete(it) }
                    Result.success(Unit)
                } else {
                    queueSync("item", itemId, "delete", "{}")
                    Result.failure(Exception(response.errorBody()?.string() ?: "Delete failed"))
                }
            } catch (e: Exception) {
                queueSync("item", itemId, "delete", "{}")
                Result.failure(e)
            }
        }
    }

    suspend fun getHealth(): Result<HealthResponse> {
        return try {
            val response = apiService.getHealth()
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
                val accessToken = appPreferences.getAccessToken() ?: return@withContext Result.failure(Exception("No access token"))
                val response = apiService.getSystemConfig("Bearer $accessToken")
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
                val accessToken = appPreferences.getAccessToken() ?: return@withContext Result.failure(Exception("No access token"))
                val response = apiService.getPurchases("Bearer $accessToken", householdId)
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
                val accessToken = appPreferences.getAccessToken() ?: return@withContext Result.failure(Exception("No access token"))
                val response = apiService.checkout("Bearer $accessToken", listId, request)
                if (response.isSuccessful) {
                    Result.success(response.body()!!)
                } else {
                    val errBody = response.errorBody()?.string()
                    if (errBody != null) {
                        try {
                            val err = json.decodeFromString<ApiError>(errBody)
                            Result.failure(Exception(err.message ?: "Checkout failed"))
                        } catch (e: Exception) {
                            Result.failure(Exception(errBody))
                        }
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
                val accessToken = appPreferences.getAccessToken() ?: return@withContext Result.failure(Exception("No access token"))
                val response = apiService.updatePurchase("Bearer $accessToken", purchaseId, request)
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
                val accessToken = appPreferences.getAccessToken() ?: return@withContext Result.failure(Exception("No access token"))
                val response = apiService.updateSystemConfig("Bearer $accessToken", config)
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

    private suspend fun queueSync(entityType: String, entityId: String, operation: String, payload: String) {
        val queueItem = SyncQueueEntity(
            entityType = entityType,
            entityId = entityId,
            operation = operation,
            payload = payload,
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
            listType = list.listType,
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
            createdAt = (item.addedAt ?: item.updatedAt).let { java.time.Instant.parse(it).toEpochMilli() },
            updatedAt = java.time.Instant.parse(item.updatedAt).toEpochMilli(),
            barcode = item.barcode,
            barcodeRaw = item.barcodeRaw,
            productId = item.productId,
            serverSynced = true
        )
    }

    private fun ShoppingListWithCounts.toUi(): ShoppingListUi {
        val icon = when (category?.lowercase()) {
            "farmacia", "farmácia", "medicamentos" -> "medication"
            "alimentos", "mercado" -> "shopping_cart"
            else -> "shopping_cart"
        }
        return ShoppingListUi(
            id = id,
            name = name,
            icon = icon,
            checkedItems = checkedCount,
            totalItems = totalCount,
            estimatedTotal = estimatedTotal,
            hasOnlineMembers = false,
            members = emptyList(),
            archived = archivedAt != null,
            isModel = listType == "MODELO",
            listType = listType
        )
    }
}