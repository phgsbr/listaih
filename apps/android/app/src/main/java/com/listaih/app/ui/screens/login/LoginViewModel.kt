package com.listaih.app.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.listaih.app.data.repository.ShoppingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: ShoppingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun login(email: String, password: String, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.login(email, password).onSuccess { loginResponse ->
                // Login successful, now get households to find the single household
                repository.getHouseholds().onSuccess { households ->
                    if (households.isNotEmpty()) {
                        val householdId = households.first().id
                        // Save householdId to preferences via repository
                        repository.saveHouseholdId(householdId)
                        _uiState.value = _uiState.value.copy(isLoading = false, isLoggedIn = true)
                        onSuccess(householdId)
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false, error = "Nenhuma casa encontrada")
                    }
                }.onFailure { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Erro ao buscar casa: ${e.message}")
                }
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Login falhou: ${e.message}")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}