package com.listaih.app.di

import com.listaih.app.data.preferences.AppPreferences
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response

class BaseUrlInterceptor(private val appPreferences: AppPreferences) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val current = request.url
        val base = appPreferences.getBaseUrl().blockingFirst().trim()
        val baseUrl = base.toHttpUrlOrNull()
        if (baseUrl == null) {
            return chain.proceed(request)
        }
        val sameHost = baseUrl.scheme == current.scheme &&
            baseUrl.host == current.host &&
            baseUrl.port == current.port
        val pathPrefix = baseUrl.encodedPath.removeSuffix("/")
        if (sameHost && pathPrefix == "") {
            return chain.proceed(request)
        }
        val newPath = if (pathPrefix.isEmpty() || current.encodedPath.startsWith(pathPrefix)) {
            current.encodedPath
        } else {
            pathPrefix + current.encodedPath
        }
        val newUrl = current.newBuilder()
            .scheme(baseUrl.scheme)
            .host(baseUrl.host)
            .port(baseUrl.port)
            .encodedPath(newPath)
            .build()
        return chain.proceed(request.newBuilder().url(newUrl).build())
    }
}