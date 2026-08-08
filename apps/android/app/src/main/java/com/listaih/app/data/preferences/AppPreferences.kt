package com.listaih.app.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.getString
import androidx.datastore.preferences.core.mutations
import androidx.datastore.preferences.core.preferencesKey
import androidx.datastore.preferences.rxjava3.RxDataStore
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder
import androidx.datastore.rxjava3.RxDataStoreBuilder
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppPreferences @Inject constructor(@Suppress("UNUSED_PARAMETER") context: Context) {

    private val dataStore: RxDataStore<Preferences> = RxPreferenceDataStoreBuilder(context, "listaih_prefs").build()

    companion object {
        val KEY_ACCESS_TOKEN = preferencesKey<String>("access_token")
        val KEY_REFRESH_TOKEN = preferencesKey<String>("refresh_token")
        val KEY_USER_ID = preferencesKey<String>("user_id")
        val KEY_USER_EMAIL = preferencesKey<String>("user_email")
        val KEY_USER_NAME = preferencesKey<String>("user_name")
        val KEY_USER_AVATAR = preferencesKey<String>("user_avatar")
        val KEY_HOUSEHOLD_ID = preferencesKey<String>("household_id")
        val KEY_HOUSEHOLD_NAME = preferencesKey<String>("household_name")
        val KEY_INVITE_CODE = preferencesKey<String>("invite_code")
        val KEY_BASE_URL = preferencesKey<String>("base_url")
        val KEY_LANGUAGE = preferencesKey<String>("language")
        val KEY_CURRENCY = preferencesKey<String>("currency")
        val KEY_THEME = preferencesKey<String>("theme")
        val KEY_NOTIFICATIONS_ENABLED = preferencesKey<Boolean>("notifications_enabled")
        val KEY_HAPTIC_FEEDBACK = preferencesKey<Boolean>("haptic_feedback")
        val KEY_OFFLINE_MODE = preferencesKey<Boolean>("offline_mode")
    }

    fun getAccessToken(): Flowable<String?> = dataStore.data.map { it[KEY_ACCESS_TOKEN] }

    fun setAccessToken(token: String?): Completable = dataStore.updateDataAsync { it.mutations { putString(KEY_ACCESS_TOKEN, token) } }

    fun getRefreshToken(): Flowable<String?> = dataStore.data.map { it[KEY_REFRESH_TOKEN] }

    fun setRefreshToken(token: String?): Completable = dataStore.updateDataAsync { it.mutations { putString(KEY_REFRESH_TOKEN, token) } }

    fun getUserId(): Flowable<String?> = dataStore.data.map { it[KEY_USER_ID] }

    fun setUserId(id: String?): Completable = dataStore.updateDataAsync { it.mutations { putString(KEY_USER_ID, id) } }

    fun getHouseholdId(): Flowable<String?> = dataStore.data.map { it[KEY_HOUSEHOLD_ID] }

    fun setHouseholdId(id: String?): Completable = dataStore.updateDataAsync { it.mutations { putString(KEY_HOUSEHOLD_ID, id) } }

    fun getBaseUrl(): Flowable<String> = dataStore.data.map { it[KEY_BASE_URL] ?: "http://10.0.2.2:3000" }

    fun setBaseUrl(url: String): Completable = dataStore.updateDataAsync { it.mutations { putString(KEY_BASE_URL, url) } }

    fun getLanguage(): Flowable<String> = dataStore.data.map { it[KEY_LANGUAGE] ?: "pt-BR" }

    fun setLanguage(lang: String): Completable = dataStore.updateDataAsync { it.mutations { putString(KEY_LANGUAGE, lang) } }

    fun getCurrency(): Flowable<String> = dataStore.data.map { it[KEY_CURRENCY] ?: "BRL" }

    fun setCurrency(currency: String): Completable = dataStore.updateDataAsync { it.mutations { putString(KEY_CURRENCY, currency) } }

    fun getTheme(): Flowable<String> = dataStore.data.map { it[KEY_THEME] ?: "system" }

    fun setTheme(theme: String): Completable = dataStore.updateDataAsync { it.mutations { putString(KEY_THEME, theme) } }

    fun getNotificationsEnabled(): Flowable<Boolean> = dataStore.data.map { it[KEY_NOTIFICATIONS_ENABLED] ?: true }

    fun setNotificationsEnabled(enabled: Boolean): Completable = dataStore.updateDataAsync { it.mutations { putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled) } }

    fun getHapticFeedback(): Flowable<Boolean> = dataStore.data.map { it[KEY_HAPTIC_FEEDBACK] ?: true }

    fun setHapticFeedback(enabled: Boolean): Completable = dataStore.updateDataAsync { it.mutations { putBoolean(KEY_HAPTIC_FEEDBACK, enabled) } }

    fun getOfflineMode(): Flowable<Boolean> = dataStore.data.map { it[KEY_OFFLINE_MODE] ?: false }

    fun setOfflineMode(enabled: Boolean): Completable = dataStore.updateDataAsync { it.mutations { putBoolean(KEY_OFFLINE_MODE, enabled) } }

    fun clearAuth(): Completable = dataStore.updateDataAsync {
        it.mutations {
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_REFRESH_TOKEN)
            remove(KEY_USER_ID)
            remove(KEY_USER_EMAIL)
            remove(KEY_USER_NAME)
            remove(KEY_USER_AVATAR)
            remove(KEY_HOUSEHOLD_ID)
            remove(KEY_HOUSEHOLD_NAME)
            remove(KEY_INVITE_CODE)
        }
    }
}