package com.listaih.app.ui.screens.purchases

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.listaih.app.data.network.model.PurchaseItem
import com.listaih.app.data.network.model.PurchaseResponse
import com.listaih.app.data.network.model.UpdatePurchaseRequest
import com.listaih.app.data.preferences.AppPreferences
import com.listaih.app.data.repository.ShoppingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PurchasesUiState(
    val purchases: List<PurchaseResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selected: PurchaseResponse? = null,
    val isSaving: Boolean = false
)

@HiltViewModel
class PurchasesViewModel @Inject constructor(
    private val repository: ShoppingRepository,
    private val preferences: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(PurchasesUiState())
    val uiState = _uiState.asStateFlow()

    private fun currentHouseholdId(): String? {
        return preferences.getHouseholdId()?.takeIf { it.isNotBlank() }
    }

    fun load() {
        val householdId = currentHouseholdId() ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.getPurchases(householdId).onSuccess { purchases ->
                _uiState.value = _uiState.value.copy(purchases = purchases, isLoading = false)
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun select(purchase: PurchaseResponse) {
        _uiState.value = _uiState.value.copy(selected = purchase)
    }

    fun refreshSelected(purchaseId: String) {
        viewModelScope.launch {
            repository.getPurchase(purchaseId).onSuccess { purchase ->
                _uiState.value = _uiState.value.copy(selected = purchase)
            }
        }
    }

    fun goBackToList() {
        _uiState.value = _uiState.value.copy(selected = null)
    }

    fun updatePurchase(paymentMethod: String?, totalAmount: Double?, notes: String?) {
        val selected = _uiState.value.selected ?: return
        _uiState.value = _uiState.value.copy(isSaving = true, error = null)
        viewModelScope.launch {
            val request = UpdatePurchaseRequest(
                paymentMethod = paymentMethod,
                totalAmount = totalAmount,
                notes = notes
            )
            repository.updatePurchase(selected.id, request).onSuccess { updated ->
                _uiState.value = _uiState.value.copy(
                    selected = updated,
                    isSaving = false
                )
                load()
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = e.message
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

fun payLabel(method: String?): String = when (method) {
    "DINHEIRO" -> "Dinheiro"
    "DEBITO" -> "Débito"
    "CREDITO" -> "Crédito"
    "PIX" -> "PIX"
    "VR" -> "VR"
    "VA" -> "VA"
    else -> "—"
}

fun receiptStatusLabel(status: String): String = when (status.lowercase()) {
    "parsed" -> "Lida"
    "processing" -> "Processando"
    "pending" -> "Pendente"
    "failed" -> "Falhou"
    else -> "Não fornecida"
}

fun purchaseItemTotal(item: PurchaseItem): Double {
    val price = item.actualPrice ?: item.estimatedPrice ?: 0.0
    return price * item.quantity
}