package com.listaih.app.data.repository

import com.listaih.app.data.local.AppDatabase
import com.listaih.app.data.local.dao.ProductDao
import com.listaih.app.data.local.entity.ProductEntity
import com.listaih.app.data.network.ApiService
import com.listaih.app.data.network.model.CreateProductRequest
import com.listaih.app.data.network.model.ProductResponse
import com.listaih.app.data.preferences.AppPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    database: AppDatabase,
    private val apiService: ApiService,
    private val appPreferences: AppPreferences
) {
    private val productDao: ProductDao = database.productDao()

    /**
     * Lookup a product by barcode. Backend falls back to Open Food Facts and
     * creates the Product automatically when found. Returns null when unknown.
     */
    suspend fun lookup(barcode: String): Result<ProductEntity?> = withContext(Dispatchers.IO) {
        try {
            val accessToken = appPreferences.getAccessToken()
                ?: return@withContext Result.failure(Exception("No access token"))
            val response = apiService.lookupProduct("Bearer $accessToken", barcode)
            if (response.isSuccessful) {
                val product = response.body()
                if (product != null) {
                    val entity = toEntity(product)
                    productDao.insert(entity)
                    Result.success(entity)
                } else {
                    Result.success(null)
                }
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Lookup failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Register a new product manually (barcode optional for generic items).
     */
    suspend fun create(
        name: String,
        barcode: String?,
        category: String?,
        defaultUnit: String = "unit"
    ): Result<ProductEntity> = withContext(Dispatchers.IO) {
        try {
            val accessToken = appPreferences.getAccessToken()
                ?: return@withContext Result.failure(Exception("No access token"))
            val response = apiService.createProduct(
                "Bearer $accessToken",
                CreateProductRequest(name, barcode, category, defaultUnit)
            )
            if (response.isSuccessful) {
                val body = response.body()
                    ?: return@withContext Result.failure(Exception("Empty response"))
                val entity = toEntity(body)
                productDao.insert(entity)
                Result.success(entity)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Create failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun toEntity(product: ProductResponse): ProductEntity {
        return ProductEntity(
            id = product.id,
            name = product.name,
            barcode = product.barcode,
            category = product.category,
            defaultUnit = product.defaultUnit,
            createdAt = java.time.Instant.parse(product.createdAt).toEpochMilli(),
            updatedAt = java.time.Instant.parse(product.updatedAt).toEpochMilli()
        )
    }
}