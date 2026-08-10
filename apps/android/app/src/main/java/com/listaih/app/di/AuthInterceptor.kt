package com.listaih.app.di

import com.listaih.app.data.network.ApiService
import com.listaih.app.data.network.model.RefreshRequest
import com.listaih.app.data.preferences.AppPreferences
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.rx3.await
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val appPreferences: AppPreferences
) : Interceptor {

    private val refreshMutex = Mutex()

    // Dedicated client to avoid the OkHttpClient -> AuthInterceptor -> Retrofit cycle
    private val apiService: ApiService by lazy {
        val client = OkHttpClient()
        Retrofit.Builder()
            .baseUrl(appPreferences.getBaseUrl().blockingFirst())
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val path = originalRequest.url.encodedPath

        // Login/refresh carry their own tokens
        if (path.endsWith("/api/auth/login") || path.endsWith("/api/auth/refresh")) {
            return chain.proceed(originalRequest)
        }

        val usedToken = appPreferences.getAccessToken() ?: return chain.proceed(originalRequest)

        // First attempt with the current access token
        val firstResponse = chain.proceed(withAuth(originalRequest, usedToken))
        if (firstResponse.code != 401) {
            return firstResponse
        }

        // Token expired: try to refresh once, then retry the request
        val refreshToken = appPreferences.getRefreshToken()
        if (refreshToken == null) {
            return firstResponse
        }
        firstResponse.close()

        val refreshed = runBlocking { refreshAccessTokenIfNeeded(usedToken) }
        if (!refreshed) {
            return chain.proceed(withAuth(originalRequest, usedToken))
        }

        val newToken = appPreferences.getAccessToken()
            ?: return chain.proceed(originalRequest)
        return chain.proceed(withAuth(originalRequest, newToken))
    }

    private fun withAuth(request: Request, token: String): Request {
        return request.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
    }

    private suspend fun refreshAccessTokenIfNeeded(usedToken: String): Boolean =
        refreshMutex.withLock {
            // Another concurrent request may have already refreshed while we waited
            if (appPreferences.getAccessToken() != usedToken) return@withLock true

            val refreshToken = appPreferences.getRefreshToken() ?: return@withLock false

            val succeeded = runCatching {
                apiService.refreshToken(RefreshRequest(refreshToken))
            }.getOrNull()?.let { response ->
                val body = response.body()
                if (response.isSuccessful && body != null && !body.accessToken.isNullOrBlank()) {
                    runBlocking {
                        appPreferences.setAccessToken(body.accessToken).await()
                        if (!body.refreshToken.isNullOrBlank()) {
                            appPreferences.setRefreshToken(body.refreshToken).await()
                        }
                    }
                    true
                } else {
                    false
                }
            } ?: false

            succeeded
        }
}