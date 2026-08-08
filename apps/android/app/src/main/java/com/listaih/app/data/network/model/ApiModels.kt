package com.listaih.app.data.network.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
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
    val updatedAt: String
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
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class CreateListRequest(
    val name: String,
    val category: String?
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
    val category: String?
)

@Serializable
data class UpdateItemRequest(
    val name: String?,
    val quantity: Double?,
    val unit: String?,
    val estimatedPrice: Double?,
    val category: String?,
    val checked: Boolean?,
    val position: Int?
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
data class HealthResponse(
    val status: String,
    val timestamp: String,
    val database: String,
    val redis: String?,
    val integrations: Map<String, String>
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
    val items: List<JsonElement>,
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