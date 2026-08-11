package com.listaih.app.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.listaih.app.data.preferences.AppPreferences
import com.listaih.app.data.repository.ShoppingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val serverUrl: String = "http://127.0.0.1:3000",
    val setupChecking: Boolean = false,
    val setupError: String? = null
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferences: AppPreferences,
    private val repository: ShoppingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState = _uiState.asStateFlow()

    private val disposables = CompositeDisposable()

    init {
        disposables.add(preferences.getBaseUrl().subscribe { _uiState.value = _uiState.value.copy(serverUrl = it) })
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

    fun saveServerUrl(url: String) {
        preferences.setBaseUrl(url).subscribe()
        _uiState.value = _uiState.value.copy(serverUrl = url)
    }

    fun testConnection(url: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            onResult(repository.testConnection(url).isSuccess)
        }
    }

    fun checkSetup(onLogin: () -> Unit, onSetup: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(setupChecking = true, setupError = null)
            repository.getSetupStatus()
                .onSuccess { status ->
                    _uiState.value = _uiState.value.copy(setupChecking = false)
                    if (status.isSetup) onLogin() else onSetup()
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(setupChecking = false, setupError = "Não foi possível consultar o servidor: ${e.message}")
                }
        }
    }
}