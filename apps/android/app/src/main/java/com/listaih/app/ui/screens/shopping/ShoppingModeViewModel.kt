package com.listaih.app.ui.screens.shopping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.listaih.app.data.network.model.CheckoutRequest
import com.listaih.app.data.network.model.UpdateItemRequest
import com.listaih.app.data.repository.ScanItemActions
import com.listaih.app.data.repository.ShoppingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ShoppingUiState(
    val listId: String,
    val listName: String,
    val items: List<ShoppingItemUi>,
    val checkedCount: Int,
    val totalCount: Int,
    val estimatedTotal: Double,
    val isLoading: Boolean = false,
    val isScanBusy: Boolean = false,
    val checkoutSuccess: Boolean = false,
    val checkoutError: String? = null,
    val showPaymentDialog: Boolean = false,
    val paymentMethod: String? = null,
    val totalAmount: Double? = null,
    val notes: String? = null,
    val syncingToGrocy: Boolean = false,
)

data class ShoppingItemUi(
    val id: String,
    val name: String,
    val quantity: Double,
    val unit: String,
    val estimatedPrice: Double?,
    val actualPrice: Double?,
    val category: String?,
    val checked: Boolean,
    val checkedBy: String?,
    val barcode: String?,
)

@HiltViewModel
class ShoppingModeViewModel @Inject constructor(
    private val repository: ShoppingRepository,
    private val scanActions: ScanItemActions,
    savedStateHandle: androidx.lifecycle.SavedStateHandle,
) : ViewModel() {

    val listId = savedStateHandle.get<String>("listId")!!
    val listName = savedStateHandle.get<String>("listName")!!

    private val _uiState = MutableStateFlow(
        ShoppingUiState(
            listId = listId,
            listName = listName,
            items = emptyList(),
            checkedCount = 0,
            totalCount = 0,
            estimatedTotal = 0.0,
        )
    )
    val uiState = _uiState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = _uiState.value
    )

    init {
        loadItems()
    }

    private fun loadItems() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.getListItems(listId).collect { entities ->
                val items = entities.map { entity ->
                    ShoppingItemUi(
                        id = entity.id,
                        name = entity.name,
                        quantity = entity.quantity,
                        unit = entity.unit,
                        estimatedPrice = entity.estimatedPrice,
                        actualPrice = entity.actualPrice,
                        category = entity.category,
                        checked = entity.checked,
                        checkedBy = entity.checkedBy,
                        barcode = entity.barcode,
                    )
                }
                val checkedCount = items.count { it.checked }
                val estimatedTotal = items.sumOf { (it.estimatedPrice ?: 0.0) * it.quantity }
                _uiState.value = _uiState.value.copy(
                    items = items,
                    checkedCount = checkedCount,
                    totalCount = items.size,
                    estimatedTotal = estimatedTotal,
                    isLoading = false,
                )
            }
        }
    }

    fun toggleCheck(itemId: String, currentlyChecked: Boolean) {
        viewModelScope.launch {
            val request = com.listaih.app.data.network.model.UpdateItemRequest(
                name = null,
                quantity = null,
                unit = null,
                estimatedPrice = null,
                category = null,
                checked = !currentlyChecked,
                position = null,
            )
            val result = repository.updateItem(itemId, listId, request)
            if (result.isSuccess) {
                loadItems()
            } else {
                _uiState.value = _uiState.value.copy(
                    checkoutError = result.exceptionOrNull()?.message
                )
            }
        }
    }

    fun refresh() {
        loadItems()
    }

    fun uncheckAll() {
        viewModelScope.launch {
            val checked = _uiState.value.items.filter { it.checked }
            checked.forEach { item ->
                val request = com.listaih.app.data.network.model.UpdateItemRequest(
                    name = null,
                    quantity = null,
                    unit = null,
                    estimatedPrice = null,
                    category = null,
                    checked = false,
                    position = null,
                )
                repository.updateItem(item.id, listId, request)
            }
            loadItems()
        }
    }

    fun openPaymentDialog() {
        val checkedItems = _uiState.value.items.filter { it.checked }
        if (checkedItems.isEmpty()) {
            _uiState.value = _uiState.value.copy(checkoutError = "Nenhum item marcado")
            return
        }
        _uiState.value = _uiState.value.copy(showPaymentDialog = true)
    }

    fun dismissPaymentDialog() {
        _uiState.value = _uiState.value.copy(
            showPaymentDialog = false,
            paymentMethod = null,
            totalAmount = null,
            notes = null,
        )
    }

    fun setPaymentMethod(method: String?) {
        _uiState.value = _uiState.value.copy(paymentMethod = method)
    }

    fun setTotalAmount(amount: Double?) {
        _uiState.value = _uiState.value.copy(totalAmount = amount)
    }

    fun setNotes(notes: String?) {
        _uiState.value = _uiState.value.copy(notes = notes)
    }

    fun setGrocySync(sync: Boolean) {
        _uiState.value = _uiState.value.copy(syncingToGrocy = sync)
    }

    fun doCheckout() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, checkoutError = null)
            val request = CheckoutRequest(
                paymentMethod = _uiState.value.paymentMethod,
                totalAmount = _uiState.value.totalAmount,
                notes = _uiState.value.notes,
                receiptPhoto = null,
                grocySync = _uiState.value.syncingToGrocy,
            )
            val result = repository.checkout(listId, request)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    checkoutSuccess = true,
                    showPaymentDialog = false,
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    checkoutError = result.exceptionOrNull()?.message,
                    showPaymentDialog = false,
                )
            }
        }
    }

    fun clearCheckoutSuccess() {
        _uiState.value = _uiState.value.copy(
            checkoutSuccess = false,
            checkoutError = null,
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(checkoutError = null)
    }

    // ---- Fase 6: post-scan popup actions ----

    fun confirmScannedItem(itemId: String, quantity: Double) {
        viewModelScope.launch {
            setScanBusy(true)
            scanActions.confirm(listId, itemId, quantity).onFailure { e ->
                _uiState.value = _uiState.value.copy(checkoutError = e.message)
            }
            setScanBusy(false)
            loadItems()
        }
    }

    fun incrementScannedItem(itemId: String, quantity: Double) {
        viewModelScope.launch {
            setScanBusy(true)
            scanActions.increment(listId, itemId, quantity).onFailure { e ->
                _uiState.value = _uiState.value.copy(checkoutError = e.message)
            }
            setScanBusy(false)
        }
    }

    fun associateScannedItem(itemId: String, barcode: String) {
        val item = _uiState.value.items.firstOrNull { it.id == itemId } ?: return
        viewModelScope.launch {
            setScanBusy(true)
            scanActions.associateBarcode(
                listId, itemId, barcode, item.name, item.category
            ).onFailure { e ->
                _uiState.value = _uiState.value.copy(checkoutError = e.message)
            }
            setScanBusy(false)
        }
    }

    fun createScannedProduct(barcode: String, name: String) {
        viewModelScope.launch {
            setScanBusy(true)
            scanActions.createScannedItem(listId, barcode, name, checked = true).onFailure { e ->
                _uiState.value = _uiState.value.copy(checkoutError = e.message)
            }
            setScanBusy(false)
            loadItems()
        }
    }

    fun createGenericScannedProduct(barcode: String) {
        viewModelScope.launch {
            setScanBusy(true)
            scanActions.createGenericItem(listId, barcode).onFailure { e ->
                _uiState.value = _uiState.value.copy(checkoutError = e.message)
            }
            setScanBusy(false)
            loadItems()
        }
    }

    suspend fun suggestScannedName(barcode: String): String? {
        return scanActions.suggestName(barcode)
    }

    private fun setScanBusy(busy: Boolean) {
        _uiState.value = _uiState.value.copy(isScanBusy = busy)
    }
}
