package com.listaih.app.data.network

import com.listaih.app.data.network.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshRequest): Response<LoginResponse>

    @POST("api/auth/logout")
    suspend fun logout(@Header("Authorization") accessToken: String): Response<Unit>

    @POST("api/auth/change-password")
    suspend fun changePassword(
        @Header("Authorization") accessToken: String,
        @Body request: ChangePasswordRequest
    ): Response<Unit>

    @GET("api/users/me")
    suspend fun getProfile(@Header("Authorization") accessToken: String): Response<UserResponse>

    @PUT("api/users/me")
    suspend fun updateProfile(
        @Header("Authorization") accessToken: String,
        @Body request: Map<String, Any>
    ): Response<UserResponse>

    @GET("api/users/households")
    suspend fun getHouseholds(@Header("Authorization") accessToken: String): Response<List<HouseholdResponse>>

    @PATCH("api/users/households/{householdId}/regenerate-code")
    suspend fun regenerateInviteCode(
        @Header("Authorization") accessToken: String,
        @Path("householdId") householdId: String
    ): Response<HouseholdResponse>

    @POST("api/users/households/join")
    suspend fun joinHousehold(
        @Header("Authorization") accessToken: String,
        @Body request: Map<String, String>
    ): Response<HouseholdResponse>

    @GET("api/households/{id}/lists")
    suspend fun getLists(
        @Header("Authorization") accessToken: String,
        @Path("id") householdId: String,
        @Query("archived") archived: Boolean? = false
    ): Response<List<ShoppingListResponse>>

    @POST("api/households/{id}/lists")
    suspend fun createList(
        @Header("Authorization") accessToken: String,
        @Path("id") householdId: String,
        @Body request: CreateListRequest
    ): Response<ShoppingListResponse>

    @GET("api/lists/{id}")
    suspend fun getList(
        @Header("Authorization") accessToken: String,
        @Path("id") listId: String
    ): Response<ShoppingListResponse>

    @PUT("api/lists/{id}")
    suspend fun updateList(
        @Header("Authorization") accessToken: String,
        @Path("id") listId: String,
        @Body request: UpdateListRequest
    ): Response<ShoppingListResponse>

    @DELETE("api/lists/{id}")
    suspend fun deleteList(
        @Header("Authorization") accessToken: String,
        @Path("id") listId: String
    ): Response<Unit>

    @POST("api/lists/{id}/items")
    suspend fun createItem(
        @Header("Authorization") accessToken: String,
        @Path("id") listId: String,
        @Body request: CreateItemRequest
    ): Response<ListItemResponse>

    @PATCH("api/lists/{listId}/items/{itemId}")
    suspend fun updateItem(
        @Header("Authorization") accessToken: String,
        @Path("listId") listId: String,
        @Path("itemId") itemId: String,
        @Body request: UpdateItemRequest
    ): Response<ListItemResponse>

    @DELETE("api/lists/{listId}/items/{itemId}")
    suspend fun deleteItem(
        @Header("Authorization") accessToken: String,
        @Path("listId") listId: String,
        @Path("itemId") itemId: String
    ): Response<Unit>

    @GET("api/products/lookup/{barcode}")
    suspend fun lookupProduct(
        @Header("Authorization") accessToken: String,
        @Path("barcode") barcode: String
    ): Response<ProductResponse?>

    @POST("api/products")
    suspend fun createProduct(
        @Header("Authorization") accessToken: String,
        @Body request: CreateProductRequest
    ): Response<ProductResponse>

    @GET("api/households/{id}/history")
    suspend fun getArchivedLists(
        @Header("Authorization") accessToken: String,
        @Path("id") householdId: String
    ): Response<List<ShoppingListResponse>>

    @GET("api/households/{id}/purchases")
    suspend fun getPurchases(
        @Header("Authorization") accessToken: String,
        @Path("id") householdId: String
    ): Response<List<PurchaseResponse>>

    @POST("api/lists/{id}/checkout")
    suspend fun checkout(
        @Header("Authorization") accessToken: String,
        @Path("id") listId: String,
        @Body request: CheckoutRequest
    ): Response<PurchaseResponse>

    @PATCH("api/purchases/{id}")
    suspend fun updatePurchase(
        @Header("Authorization") accessToken: String,
        @Path("id") purchaseId: String,
        @Body request: UpdatePurchaseRequest
    ): Response<PurchaseResponse>

    @GET("api/purchases/{id}")
    suspend fun getPurchase(
        @Header("Authorization") accessToken: String,
        @Path("id") purchaseId: String
    ): Response<PurchaseResponse>

    @GET("api/system/config")
    suspend fun getSystemConfig(@Header("Authorization") accessToken: String): Response<SystemConfigResponse>

    @PUT("api/system/config")
    suspend fun updateSystemConfig(
        @Header("Authorization") accessToken: String,
        @Body request: Map<String, Any>
    ): Response<SystemConfigResponse>

    @GET("api/health")
    suspend fun getHealth(): Response<HealthResponse>

    @GET("api/setup/status")
    suspend fun getSetupStatus(): Response<Map<String, Any>>

    @POST("api/setup")
    suspend fun setup(@Body request: Map<String, Any>): Response<LoginResponse>
}