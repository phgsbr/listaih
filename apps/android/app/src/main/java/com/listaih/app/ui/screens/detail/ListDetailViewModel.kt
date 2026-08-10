package com.listaih.app.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.listaih.app.data.local.entity.ListItemEntity
import com.listaih.app.data.network.model.UpdateItemRequest
import com.listaih.app.data.repository.ScanItemActions
import com.listaih.app.data.repository.ShoppingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ListDetailUiState(
    val items: List<ListItemUi> = emptyList(),
    val isLoading: Boolean = false,
    val isScanBusy: Boolean = false,
    val error: String? = null,
    val title: String = ""
)

@HiltViewModel
class ListDetailViewModel @Inject constructor(
    private val repository: ShoppingRepository,
    private val scanActions: ScanItemActions,
    savedStateHandle: androidx.lifecycle.SavedStateHandle,
) : ViewModel() {

    val listId = savedStateHandle.get<String>("listId") ?: ""
    val listName = savedStateHandle.get<String>("listName") ?: ""

    private val _uiState = MutableStateFlow(ListDetailUiState(title = listName))
    val uiState = _uiState.asStateFlow()

    init {
        observeItems()
        viewModelScope.launch {
            repository.syncListItems(listId).onFailure { e ->
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    private fun observeItems() {
        viewModelScope.launch {
            repository.getListItems(listId).collect { entities ->
                _uiState.value = _uiState.value.copy(
                    items = entities.map { it.toUi() },
                    isLoading = false
                )
            }
        }
    }

    fun toggleCheck(itemId: String, currentlyChecked: Boolean) {
        viewModelScope.launch {
            val request = UpdateItemRequest(
                name = null,
                quantity = null,
                unit = null,
                estimatedPrice = null,
                category = null,
                checked = !currentlyChecked,
                position = null,
            )
            repository.updateItem(itemId, listId, request).onFailure { e ->
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun checkAll() {
        val toCheck = _uiState.value.items.filter { !it.checked }
        toCheck.forEach { item ->
            viewModelScope.launch {
                val request = UpdateItemRequest(
                    name = null,
                    quantity = null,
                    unit = null,
                    estimatedPrice = null,
                    category = null,
                    checked = true,
                    position = null,
                )
                repository.updateItem(item.id, listId, request)
            }
        }
    }

    fun uncheckAll() {
        val toUncheck = _uiState.value.items.filter { it.checked }
        toUncheck.forEach { item ->
            viewModelScope.launch {
                val request = UpdateItemRequest(
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
        }
    }

    fun deleteChecked() {
        val checked = _uiState.value.items.filter { it.checked }
        checked.forEach { item ->
            viewModelScope.launch {
                repository.deleteItem(item.id, listId)
            }
        }
    }

    fun deleteItem(itemId: String) {
        viewModelScope.launch {
            repository.deleteItem(itemId, listId).onFailure { e ->
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun addItem(name: String, quantity: Double, unit: String, price: Double?, category: String?) {
        viewModelScope.launch {
            repository.createItem(listId, name, quantity, unit, price, category).onFailure { e ->
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun updateItem(itemId: String, name: String, quantity: Double, unit: String, price: Double?, category: String) {
        viewModelScope.launch {
            val request = UpdateItemRequest(
                name = name,
                quantity = quantity,
                unit = unit,
                estimatedPrice = price,
                category = category,
                checked = null,
                position = null,
            )
            repository.updateItem(itemId, listId, request).onFailure { e ->
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun renameList(name: String) {
        if (name.isBlank()) return
        _uiState.value = _uiState.value.copy(title = name)
        viewModelScope.launch {
            repository.updateList(listId, name, null, null).onFailure { e ->
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    // ---- Fase 6: post-scan popup actions ----

    fun confirmScannedItem(itemId: String, quantity: Double) {
        viewModelScope.launch {
            setScanBusy(true)
            scanActions.confirm(listId, itemId, quantity).onFailure { e ->
                _uiState.value = _uiState.value.copy(error = e.message)
            }
            setScanBusy(false)
        }
    }

    fun incrementScannedItem(itemId: String, quantity: Double) {
        viewModelScope.launch {
            setScanBusy(true)
            scanActions.increment(listId, itemId, quantity).onFailure { e ->
                _uiState.value = _uiState.value.copy(error = e.message)
            }
            setScanBusy(false)
        }
    }

    fun associateScannedItem(itemId: String, barcode: String) {
        val item = _uiState.value.items.firstOrNull { it.id == itemId } ?: return
        viewModelScope.launch {
            setScanBusy(true)
            scanActions.associateBarcode(
                listId, itemId, barcode, item.name, item.category.takeIf { it != "Sem Categoria" }
            ).onFailure { e ->
                _uiState.value = _uiState.value.copy(error = e.message)
            }
            setScanBusy(false)
        }
    }

    fun createScannedProduct(barcode: String, name: String) {
        viewModelScope.launch {
            setScanBusy(true)
            scanActions.createScannedItem(listId, barcode, name, checked = true).onFailure { e ->
                _uiState.value = _uiState.value.copy(error = e.message)
            }
            setScanBusy(false)
        }
    }

    fun createGenericScannedProduct(barcode: String) {
        viewModelScope.launch {
            setScanBusy(true)
            scanActions.createGenericItem(listId, barcode).onFailure { e ->
                _uiState.value = _uiState.value.copy(error = e.message)
            }
            setScanBusy(false)
        }
    }

    suspend fun suggestScannedName(barcode: String): String? {
        return scanActions.suggestName(barcode)
    }

    private fun setScanBusy(busy: Boolean) {
        _uiState.value = _uiState.value.copy(isScanBusy = busy)
    }

    fun refresh() {
        viewModelScope.launch {
            repository.syncListItems(listId).onFailure { e ->
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun ListItemEntity.toUi(): ListItemUi {
        val qty = if (quantity == quantity.toLong().toDouble()) {
            quantity.toLong().toString()
        } else {
            String.format("%.2f", quantity).trimEnd('0').trimEnd('.')
        }
        return ListItemUi(
            id = id,
            name = name,
            quantity = "$qty $unit",
            estimatedPrice = estimatedPrice,
            actualPrice = actualPrice,
            category = category ?: "Sem Categoria",
            checked = checked,
            checkedBy = checkedBy,
            barcode = barcode,
        )
    }
}