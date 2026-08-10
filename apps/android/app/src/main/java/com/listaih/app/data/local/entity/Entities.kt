package com.listaih.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "shopping_lists")
data class ShoppingListEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String?,
    val householdId: String,
    val archivedAt: Long?,
    val createdAt: Long,
    val updatedAt: Long,
    val listType: String = "PONTUAL",
    val serverSynced: Boolean = false,
    val localVersion: Int = 0
)

@Serializable
@Entity(tableName = "list_items")
data class ListItemEntity(
    @PrimaryKey val id: String,
    val listId: String,
    val name: String,
    val quantity: Double,
    val unit: String,
    val estimatedPrice: Double?,
    val actualPrice: Double?,
    val category: String?,
    val checked: Boolean,
    val checkedBy: String?,
    val checkedAt: Long?,
    val position: Int,
    val createdAt: Long,
    val updatedAt: Long,
    val barcode: String? = null,
    val barcodeRaw: String? = null,
    val productId: String? = null,
    val serverSynced: Boolean = false,
    val localVersion: Int = 0
)

@Serializable
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val barcode: String?,
    val category: String?,
    val defaultUnit: String,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
@Entity(tableName = "price_entries")
data class PriceEntryEntity(
    @PrimaryKey val id: String,
    val productId: String,
    val storeName: String?,
    val price: Double,
    val date: Long,
    val userId: String,
    val createdAt: Long
)

@Serializable
@Entity(tableName = "households")
data class HouseholdEntity(
    @PrimaryKey val id: String,
    val name: String,
    val inviteCode: String,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val email: String,
    val name: String,
    val avatar: String?,
    val provider: String,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val entityType: String, // "list", "item", "product"
    val entityId: String,
    val operation: String, // "create", "update", "delete"
    val payload: String, // JSON
    val createdAt: Long,
    val retryCount: Int = 0
)

data class ShoppingListWithCounts(
    val id: String,
    val name: String,
    val category: String?,
    val householdId: String,
    val archivedAt: Long?,
    val createdAt: Long,
    val updatedAt: Long,
    val listType: String,
    val serverSynced: Boolean,
    val localVersion: Int,
    val checkedCount: Int,
    val totalCount: Int,
    val spentTotal: Double,
    val estimatedTotal: Double
)