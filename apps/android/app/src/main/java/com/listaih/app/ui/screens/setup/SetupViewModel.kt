package com.listaih.app.ui.screens.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.listaih.app.data.network.model.SetupRequest
import com.listaih.app.data.repository.ShoppingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SetupUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val repository: ShoppingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SetupUiState())
    val uiState = _uiState.asStateFlow()

    fun runSetup(name: String, email: String, password: String, householdName: String, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.runSetup(SetupRequest(name, email, password, householdName))
                .onSuccess {
                    repository.login(email, password)
                        .onSuccess { loginResponse ->
                            repository.getHouseholds().onSuccess { households ->
                                if (households.isNotEmpty()) {
                                    val householdId = households.first().id
                                    repository.saveHouseholdId(householdId)
                                    _uiState.value = _uiState.value.copy(isLoading = false)
                                    onSuccess(householdId)
                                } else {
                                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Nenhuma casa encontrada")
                                }
                            }.onFailure { e ->
                                _uiState.value = _uiState.value.copy(isLoading = false, error = "Erro ao buscar casa: ${e.message}")
                            }
                        }
                        .onFailure { e ->
                            _uiState.value = _uiState.value.copy(isLoading = false, error = "Conta criada, mas o login falhou: ${e.message}")
                        }
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Não foi possível criar a conta: ${e.message}")
                }
        }
    }
}