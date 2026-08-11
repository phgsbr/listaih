package com.listaih.app.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder
import androidx.datastore.rxjava3.RxDataStore
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppPreferences @Inject constructor(context: Context) {

    private val dataStore: RxDataStore<Preferences> =
        RxPreferenceDataStoreBuilder(context, "listaih_prefs").build()

    companion object {
        val KEY_ACCESS_TOKEN = stringPreferencesKey("access_token")
        val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val KEY_USER_ID = stringPreferencesKey("user_id")
        val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        val KEY_USER_NAME = stringPreferencesKey("user_name")
        val KEY_USER_AVATAR = stringPreferencesKey("user_avatar")
        val KEY_HOUSEHOLD_ID = stringPreferencesKey("household_id")
        val KEY_HOUSEHOLD_NAME = stringPreferencesKey("household_name")
        val KEY_INVITE_CODE = stringPreferencesKey("invite_code")
        val KEY_BASE_URL = stringPreferencesKey("base_url")
        val KEY_LANGUAGE = stringPreferencesKey("language")
        val KEY_CURRENCY = stringPreferencesKey("currency")
        val KEY_THEME = stringPreferencesKey("theme")
        val KEY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val KEY_HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
        val KEY_OFFLINE_MODE = booleanPreferencesKey("offline_mode")
        val KEY_WEAR_SCAN_DETAIL = booleanPreferencesKey("wear_scan_detail")
    }

    fun getAccessToken(): String? = dataStore.data().blockingFirst()[KEY_ACCESS_TOKEN]

    fun setAccessToken(token: String?): Completable = update {
        if (token == null) it.remove(KEY_ACCESS_TOKEN) else it[KEY_ACCESS_TOKEN] = token
    }

    fun getRefreshToken(): String? = dataStore.data().blockingFirst()[KEY_REFRESH_TOKEN]

    fun setRefreshToken(token: String?): Completable = update {
        if (token == null) it.remove(KEY_REFRESH_TOKEN) else it[KEY_REFRESH_TOKEN] = token
    }

    fun getUserId(): String? = dataStore.data().blockingFirst()[KEY_USER_ID]

    fun setUserId(id: String?): Completable = update {
        if (id == null) it.remove(KEY_USER_ID) else it[KEY_USER_ID] = id
    }

    fun getUserName(): String? = dataStore.data().blockingFirst()[KEY_USER_NAME]

    fun setUserName(name: String?): Completable = update {
        if (name == null) it.remove(KEY_USER_NAME) else it[KEY_USER_NAME] = name
    }

    fun getUserEmail(): String? = dataStore.data().blockingFirst()[KEY_USER_EMAIL]

    fun setUserEmail(email: String?): Completable = update {
        if (email == null) it.remove(KEY_USER_EMAIL) else it[KEY_USER_EMAIL] = email
    }

    fun getHouseholdId(): String? = dataStore.data().blockingFirst()[KEY_HOUSEHOLD_ID]

    fun getHouseholdIdFlow(): Flowable<String> = dataStore.data().map { it[KEY_HOUSEHOLD_ID] ?: "" }

    fun setHouseholdId(id: String?): Completable = update {
        if (id == null) it.remove(KEY_HOUSEHOLD_ID) else it[KEY_HOUSEHOLD_ID] = id
    }

    fun getHouseholdName(): String? = dataStore.data().blockingFirst()[KEY_HOUSEHOLD_NAME]

    fun setHouseholdName(name: String?): Completable = update {
        if (name == null) it.remove(KEY_HOUSEHOLD_NAME) else it[KEY_HOUSEHOLD_NAME] = name
    }

    fun getInviteCode(): String? = dataStore.data().blockingFirst()[KEY_INVITE_CODE]

    fun setInviteCode(code: String?): Completable = update {
        if (code == null) it.remove(KEY_INVITE_CODE) else it[KEY_INVITE_CODE] = code
    }

    fun getBaseUrl(): Flowable<String> = dataStore.data().map { it[KEY_BASE_URL] ?: "http://127.0.0.1:3000" }

    fun setBaseUrl(url: String): Completable = update { it[KEY_BASE_URL] = url }

    fun getLanguage(): Flowable<String> = dataStore.data().map { it[KEY_LANGUAGE] ?: "pt-BR" }

    fun setLanguage(lang: String): Completable = update { it[KEY_LANGUAGE] = lang }

    fun getCurrency(): Flowable<String> = dataStore.data().map { it[KEY_CURRENCY] ?: "BRL" }

    fun setCurrency(currency: String): Completable = update { it[KEY_CURRENCY] = currency }

    fun getTheme(): Flowable<String> = dataStore.data().map { it[KEY_THEME] ?: "system" }

    fun setTheme(theme: String): Completable = update { it[KEY_THEME] = theme }

    fun getNotificationsEnabled(): Flowable<Boolean> = dataStore.data().map { it[KEY_NOTIFICATIONS_ENABLED] ?: true }

    fun setNotificationsEnabled(enabled: Boolean): Completable = update { it[KEY_NOTIFICATIONS_ENABLED] = enabled }

    fun getHapticFeedback(): Flowable<Boolean> = dataStore.data().map { it[KEY_HAPTIC_FEEDBACK] ?: true }

    fun setHapticFeedback(enabled: Boolean): Completable = update { it[KEY_HAPTIC_FEEDBACK] = enabled }

    fun getOfflineMode(): Flowable<Boolean> = dataStore.data().map { it[KEY_OFFLINE_MODE] ?: false }

    fun setOfflineMode(enabled: Boolean): Completable = update { it[KEY_OFFLINE_MODE] = enabled }

    fun getWearScanDetail(): Flowable<Boolean> = dataStore.data().map { it[KEY_WEAR_SCAN_DETAIL] ?: false }

    fun setWearScanDetail(enabled: Boolean): Completable = update { it[KEY_WEAR_SCAN_DETAIL] = enabled }

    fun clearAuth(): Completable = update {
        it.remove(KEY_ACCESS_TOKEN)
        it.remove(KEY_REFRESH_TOKEN)
        it.remove(KEY_USER_ID)
        it.remove(KEY_USER_EMAIL)
        it.remove(KEY_USER_NAME)
        it.remove(KEY_USER_AVATAR)
        it.remove(KEY_HOUSEHOLD_ID)
        it.remove(KEY_HOUSEHOLD_NAME)
        it.remove(KEY_INVITE_CODE)
    }

    private fun update(transform: (MutablePreferences) -> Unit): Completable {
        return dataStore.updateDataAsync { preferences ->
            Single.fromCallable {
                preferences.toMutablePreferences().apply(transform).toPreferences()
            }
        }.ignoreElement()
    }
}
