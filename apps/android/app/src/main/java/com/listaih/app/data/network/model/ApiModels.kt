package com.listaih.app.data.network.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class RefreshRequest(
    val refreshToken: String
)

@Serializable
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

@Serializable
data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: UserResponse
)

@Serializable
data class UserResponse(
    val id: String,
    val email: String,
    val name: String,
    val avatar: String?,
    val provider: String
)

@Serializable
data class HouseholdResponse(
    val id: String,
    val name: String,
    val inviteCode: String,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class ShoppingListResponse(
    val id: String,
    val name: String,
    val category: String?,
    val archivedAt: String?,
    val items: List<ListItemResponse> = emptyList(),
    val householdId: String,
    val createdAt: String,
    val updatedAt: String,
    val listType: String = "PONTUAL"
)

@Serializable
data class ListItemResponse(
    val id: String,
    val listId: String,
    val name: String,
    val quantity: Double,
    val unit: String,
    val estimatedPrice: Double?,
    val actualPrice: Double?,
    val category: String?,
    val checked: Boolean,
    val checkedBy: String?,
    val checkedAt: String?,
    val position: Int,
    val addedAt: String?,
    val updatedAt: String,
    val barcode: String? = null,
    val barcodeRaw: String? = null,
    val productId: String? = null
)

@Serializable
data class CreateListRequest(
    val name: String,
    val category: String?,
    val listType: String? = null
)

@Serializable
data class UpdateListRequest(
    val name: String?,
    val category: String?,
    val archivedAt: String?
)

@Serializable
data class CreateItemRequest(
    val name: String,
    val quantity: Double,
    val unit: String,
    val estimatedPrice: Double?,
    val category: String?,
    val barcode: String? = null,
    val barcodeRaw: String? = null,
    val productId: String? = null
)

@Serializable
data class UpdateItemRequest(
    val name: String?,
    val quantity: Double?,
    val unit: String?,
    val estimatedPrice: Double?,
    val category: String?,
    val checked: Boolean?,
    val position: Int?,
    val barcode: String? = null,
    val barcodeRaw: String? = null,
    val productId: String? = null
)

@Serializable
data class ProductBarcodeResponse(
    val id: String,
    val productId: String,
    val barcode: String,
    val createdAt: String
)

@Serializable
data class ProductResponse(
    val id: String,
    val name: String,
    val barcode: String?,
    val category: String?,
    val defaultUnit: String,
    val createdAt: String,
    val updatedAt: String,
    val barcodes: List<ProductBarcodeResponse> = emptyList()
)

@Serializable
data class CreateProductRequest(
    val name: String,
    val barcode: String? = null,
    val category: String? = null,
    val defaultUnit: String? = null
)

@Serializable
data class SystemConfigResponse(
    val isSetup: Boolean,
    val installedAt: String?,
    val currency: String,
    val grocyUrl: String?,
    val grocyApiKey: String?,
    val grocyEnabled: Boolean,
    val haUrl: String?,
    val haWebhookToken: String?,
    val haEnabled: Boolean,
    val apiEnabled: Boolean,
    val apiBaseUrl: String?,
    val apiKey: String?
)

 @Serializable
data class IntegrationStatus(
    val enabled: Boolean = false,
    val url: String? = null
)

@Serializable
data class HealthResponse(
    val status: String,
    val timestamp: String,
    val database: String,
    val redis: String?,
    val integrations: Map<String, IntegrationStatus> = emptyMap()
)

@Serializable
data class SetupStatusResponse(
    val isSetup: Boolean,
    val installedAt: String? = null
)

@Serializable
data class SetupRequest(
    val name: String,
    val email: String,
    val password: String,
    val householdName: String
)

@Serializable
data class SetupAdmin(
    val id: String,
    val email: String,
    val name: String
)

@Serializable
data class SetupHousehold(
    val id: String,
    val name: String
)

@Serializable
data class SetupResponse(
    val message: String,
    val admin: SetupAdmin? = null,
    val household: SetupHousehold? = null
)

@Serializable
data class PurchaseItem(
    val id: String? = null,
    val name: String,
    val quantity: Double,
    val unit: String? = null,
    val estimatedPrice: Double? = null,
    val actualPrice: Double? = null,
    val category: String? = null,
    val notes: String? = null,
    val barcode: String? = null,
    val barcodeData: String? = null,
    val checked: Boolean? = null,
    val checkedAt: String? = null
)

@Serializable
data class PurchaseResponse(
    val id: String,
    val listId: String,
    val householdId: String,
    val userId: String,
    val date: String,
    val totalAmount: Double?,
    val paymentMethod: String?,
    val notes: String?,
    val receiptPhoto: String?,
    val receiptParsed: Map<String, @Contextual kotlin.Any>?,
    val receiptStatus: String,
    val itemCount: Int,
    val items: List<PurchaseItem> = emptyList(),
    val grocySynced: Boolean,
    val grocySyncedAt: String?,
    val createdAt: String,
    val updatedAt: String,
    val list: ListItemResponse? = null
)

@Serializable
data class CheckoutRequest(
    val paymentMethod: String? = null,
    val totalAmount: Double? = null,
    val notes: String? = null,
    val receiptPhoto: String? = null,
    val grocySync: Boolean? = null
)

@Serializable
data class UpdatePurchaseRequest(
    val paymentMethod: String? = null,
    val totalAmount: Double? = null,
    val notes: String? = null,
    val receiptPhoto: String? = null,
    val receiptParsed: Map<String, @Contextual kotlin.Any>? = null,
    val receiptStatus: String? = null
)

@Serializable
data class ApiError(
    val statusCode: Int,
    val message: String,
    val error: String
)