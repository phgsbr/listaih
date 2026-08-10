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

    private val _darkTheme = MutableStateFlow(false)
    val darkTheme = _darkTheme.asStateFlow()

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

    private val _userName = MutableStateFlow("")
    val userName = _userName.asStateFlow()

    private val _userEmail = MutableStateFlow("")
    val userEmail = _userEmail.asStateFlow()

    private val _householdName = MutableStateFlow("")
    val householdName = _householdName.asStateFlow()

    private val _inviteCode = MutableStateFlow("")
    val inviteCode = _inviteCode.asStateFlow()

    private val _householdId = MutableStateFlow<String?>(null)
    val householdId = _householdId.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    private val disposables = CompositeDisposable()

    init {
        disposables.add(preferences.getTheme().subscribe { value -> _darkTheme.value = value == "dark" })
        disposables.add(preferences.getNotificationsEnabled().subscribe { _notificationsEnabled.value = it })
        disposables.add(preferences.getHapticFeedback().subscribe { _hapticFeedback.value = it })
        disposables.add(preferences.getOfflineMode().subscribe { _offlineMode.value = it })
        disposables.add(preferences.getLanguage().subscribe { _language.value = it })
        disposables.add(preferences.getCurrency().subscribe { _currency.value = it })

        _userName.value = preferences.getUserName() ?: ""
        _userEmail.value = preferences.getUserEmail() ?: ""
        _householdName.value = preferences.getHouseholdName() ?: ""
        _inviteCode.value = preferences.getInviteCode() ?: ""
        _householdId.value = preferences.getHouseholdId()?.takeIf { it.isNotBlank() } ?: ""

        loadFromServer()
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

    fun loadFromServer() {
        viewModelScope.launch {
            _loading.value = true

            repository.getProfile().onSuccess { profile: UserResponse ->
                _userName.value = profile.name
                _userEmail.value = profile.email
            }.onFailure { e ->
                _message.value = "Não foi possível carregar o perfil: ${e.message}"
            }

            repository.getHouseholds().onSuccess { households ->
                val currentId = _householdId.value
                val current = households.firstOrNull { it.id == currentId } ?: households.firstOrNull()
                if (current != null) {
                    _householdId.value = current.id
                    _householdName.value = current.name
                    _inviteCode.value = current.inviteCode
                    preferences.setHouseholdId(current.id).subscribe()
                    preferences.setHouseholdName(current.name).subscribe()
                    preferences.setInviteCode(current.inviteCode).subscribe()
                }
            }.onFailure { e ->
                _message.value = "Não foi possível carregar a casa: ${e.message}"
            }

            _loading.value = false
        }
    }

    fun regenerateInviteCode() {
        val id = _householdId.value ?: return
        viewModelScope.launch {
            _loading.value = true
            repository.regenerateInviteCode(id).onSuccess { household ->
                _inviteCode.value = household.inviteCode
                _householdName.value = household.name
                preferences.setInviteCode(household.inviteCode).subscribe()
                _message.value = "Código de convite atualizado"
            }.onFailure { e ->
                _message.value = "Não foi possível gerar um novo código: ${e.message}"
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

    fun exportData(onResult: (String?) -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            repository.exportLocalData().onSuccess { json ->
                onResult(json)
            }.onFailure { e ->
                _message.value = "Erro ao exportar dados: ${e.message}"
            }
            _loading.value = false
        }
    }

    fun swipeMessage() {
        _message.value = null
    }

    fun setDarkTheme(enabled: Boolean) {
        _darkTheme.value = enabled
        preferences.setTheme(if (enabled) "dark" else "light").subscribe()
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
}