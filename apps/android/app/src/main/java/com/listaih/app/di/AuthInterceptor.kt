package com.listaih.app.di

import android.util.Log
import com.listaih.app.data.preferences.AppPreferences
import io.reactivex.rxjava3.core.Completable
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val appPreferences: AppPreferences
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val accessToken = appPreferences.getAccessToken().blockingFirst()

        val requestBuilder = originalRequest.newBuilder()

        accessToken?.let { token ->
            requestBuilder.header("Authorization", "Bearer $token")
        }

        val newRequest = requestBuilder.build()
        return chain.proceed(newRequest)
    }
}