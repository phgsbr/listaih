package com.listaih.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.listaih.app.data.network.model.UserResponse
import com.listaih.app.data.preferences.AppPreferences
import com.listaih.app.data.repository.ShoppingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferences: AppPreferences,
    private val repository: ShoppingRepository
) : ViewModel() {

    private val _theme = MutableStateFlow("system")
    val theme = _theme.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled = _notificationsEnabled.asStateFlow()

    private val _hapticFeedback = MutableStateFlow(true)
    val hapticFeedback = _hapticFeedback.asStateFlow()

    private val _offlineMode = MutableStateFlow(false)
    val offlineMode = _offlineMode.asStateFlow()

    private val _language = MutableStateFlow("pt-BR")
    val language = _language.asStateFlow()

    private val _currency = MutableStateFlow("BRL")
    val currency = _currency.asStateFlow()

    private val _wearScanDetail = MutableStateFlow(false)
    val wearScanDetail = _wearScanDetail.asStateFlow()

    private val _baseUrl = MutableStateFlow("http://127.0.0.1:3000")
    val baseUrl = _baseUrl.asStateFlow()

    private val _userName = MutableStateFlow("")
    val userName = _userName.asStateFlow()

    private val _userEmail = MutableStateFlow("")
    val userEmail = _userEmail.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    private val disposables = CompositeDisposable()

    init {
        disposables.add(preferences.getTheme().subscribe { _theme.value = it })
        disposables.add(preferences.getNotificationsEnabled().subscribe { _notificationsEnabled.value = it })
        disposables.add(preferences.getHapticFeedback().subscribe { _hapticFeedback.value = it })
        disposables.add(preferences.getOfflineMode().subscribe { _offlineMode.value = it })
        disposables.add(preferences.getLanguage().subscribe { _language.value = it })
        disposables.add(preferences.getCurrency().subscribe { _currency.value = it })
        disposables.add(preferences.getWearScanDetail().subscribe { _wearScanDetail.value = it })
        disposables.add(preferences.getBaseUrl().subscribe { _baseUrl.value = it })

        _userName.value = preferences.getUserName() ?: ""
        _userEmail.value = preferences.getUserEmail() ?: ""

        loadProfile()
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _loading.value = true
            repository.getProfile().onSuccess { profile: UserResponse ->
                _userName.value = profile.name
                _userEmail.value = profile.email
            }.onFailure { e ->
                _message.value = "Não foi possível carregar o perfil: ${e.message}"
            }
            _loading.value = false
        }
    }

    fun changePassword(current: String, new: String) {
        viewModelScope.launch {
            _loading.value = true
            repository.changePassword(current, new).onSuccess {
                _message.value = "Senha alterada com sucesso"
            }.onFailure { e ->
                _message.value = "Erro ao alterar a senha: ${e.message}"
            }
            _loading.value = false
        }
    }

    fun testConnection(url: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            onResult(repository.testConnection(url).isSuccess)
            _loading.value = false
        }
    }

    fun saveBaseUrl(url: String) {
        _baseUrl.value = url
        preferences.setBaseUrl(url).subscribe()
        _message.value = "Servidor atualizado. A próxima conexão usará a nova URL."
    }

    fun swipeMessage() {
        _message.value = null
    }

    fun setTheme(value: String) {
        _theme.value = value
        preferences.setTheme(value).subscribe()
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        _notificationsEnabled.value = enabled
        preferences.setNotificationsEnabled(enabled).subscribe()
    }

    fun setHapticFeedback(enabled: Boolean) {
        _hapticFeedback.value = enabled
        preferences.setHapticFeedback(enabled).subscribe()
    }

    fun setOfflineMode(enabled: Boolean) {
        _offlineMode.value = enabled
        preferences.setOfflineMode(enabled).subscribe()
    }

    fun setLanguage(lang: String) {
        _language.value = lang
        preferences.setLanguage(lang).subscribe()
    }

    fun setCurrency(currency: String) {
        _currency.value = currency
        preferences.setCurrency(currency).subscribe()
    }

    fun setWearScanDetail(enabled: Boolean) {
        _wearScanDetail.value = enabled
        preferences.setWearScanDetail(enabled).subscribe()
    }
}