package com.listaih.wear

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WearItemUi(
    val id: String,
    val name: String,
    val quantity: Double,
    val unit: String,
    val estimatedPrice: Double?,
    val category: String,
    val barcode: String?,
    val checked: Boolean,
    val extraBarcodes: List<String> = emptyList()
)

data class WearMainUiState(
    val currentListId: String? = null,
    val items: List<WearItemUi> = mockItems
)

sealed class WearScanPopup {
    data class Recognized(val barcode: String, val item: WearItemUi) : WearScanPopup()
    data class Unrecognized(val barcode: String) : WearScanPopup()
}

private val mockItems = listOf(
    WearItemUi("1", "Tomate", 2.0, "kg", 6.99, "Hortifruti", "7891000100103", false),
    WearItemUi("2", "Alface", 1.0, "un", 3.49, "Hortifruti", "7891000100110", true),
    WearItemUi("3", "Cenoura", 0.5, "kg", 4.99, "Hortifruti", "7891000100127", false),
    WearItemUi("4", "Leite", 2.0, "un", 5.49, "Laticínios", "7891000100134", true),
    WearItemUi("5", "Queijo", 0.2, "kg", 39.90, "Laticínios", "7891000100141", false),
    WearItemUi("6", "Pão", 1.0, "un", 8.90, "Padaria", "7891000100158", false),
    WearItemUi("7", "Arroz", 5.0, "kg", 4.29, "Sem Categoria", "7891000100165", false)
)

@HiltViewModel
class WearMainViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _currentListId = MutableStateFlow<String?>(null)
    val currentListId = _currentListId.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected = _isConnected.asStateFlow()

    private val _uiState = MutableStateFlow(WearMainUiState())
    val uiState = _uiState.asStateFlow()

    private val _scanPopup = MutableStateFlow<WearScanPopup?>(null)
    val scanPopup = _scanPopup.asStateFlow()

    private val scanBuffer = StringBuilder()
    private var lastKeyTime = 0L

    init {
        checkConnection()
    }

    private fun checkConnection() {
        viewModelScope.launch {
            // TODO: Data Layer API (Fase 9.1) — Phone → Watch envia lista ativa + progresso
            _isConnected.value = true
        }
    }

    fun setCurrentList(listId: String?) {
        _currentListId.value = listId
    }

    // ---- Itens (skeleton — mock; Fase 9.1 substitui por dados do Phone) ----

    fun toggleItem(id: String) {
        _uiState.update { state ->
            state.copy(
                items = state.items.map { item ->
                    if (item.id == id) item.copy(checked = !item.checked) else item
                }
            )
        }
    }

    fun updateItemQuantity(id: String, quantity: Double) {
        if (quantity <= 0.0) return
        _uiState.update { state ->
            state.copy(
                items = state.items.map { item ->
                    if (item.id == id) item.copy(quantity = quantity) else item
                }
            )
        }
    }

    fun updateItemPrice(id: String, price: Double?) {
        _uiState.update { state ->
            state.copy(
                items = state.items.map { item ->
                    if (item.id == id) item.copy(estimatedPrice = price) else item
                }
            )
        }
    }

    // ---- Scanner BT (HID) ----
    // Sem toggle no relógio: o controle do scanner é feito no app do celular (Fase 9 full)

    /** Retorna true se consumiu a tecla (scanner ativo). */
    fun onHidKey(keyCode: Int, eventTime: Long): Boolean {
        return when (keyCode) {
            in 7..16 -> {
                if (scanBuffer.isEmpty() || eventTime - lastKeyTime > 250) {
                    scanBuffer.setLength(0)
                }
                scanBuffer.append((keyCode - 7).toString())
                lastKeyTime = eventTime
                true
            }
            // Comparação literal: sparse-switch do compilador rejeita KEYCODE_* aqui
            66, 134 -> {
                if (scanBuffer.length >= 4) {
                    val barcode = scanBuffer.toString()
                    scanBuffer.setLength(0)
                    handleScan(barcode)
                }
                true
            }
            else -> false
        }
    }

    private fun handleScan(barcode: String) {
        // Popup só faz sentido durante a compra (lista ativa)
        if (_currentListId.value == null) return
        val current = _scanPopup.value
        if (current is WearScanPopup.Recognized && current.barcode == barcode) {
            // Scan repetido do mesmo código → +1 na quantidade (popup continua aberto)
            val item = _uiState.value.items.firstOrNull { it.barcode == barcode }
            if (item != null) {
                updateItemQuantity(item.id, item.quantity + 1.0)
                _scanPopup.value = WearScanPopup.Recognized(
                    barcode = barcode,
                    item = _uiState.value.items.first { it.id == item.id }
                )
            }
            return
        }
        // Novo scan → confirma o anterior (sem timer, plan §6.8) e mostra o novo
        if (current != null) confirmPrevious()
        val item = _uiState.value.items.firstOrNull {
            it.barcode == barcode || it.extraBarcodes.contains(barcode)
        }
        _scanPopup.value = if (item != null) {
            WearScanPopup.Recognized(barcode, item)
        } else {
            WearScanPopup.Unrecognized(barcode)
        }
    }

    /** Associa um código ainda não reconhecido a um produto existente da lista. */
    fun associateBarcode(itemId: String, barcode: String) {
        val item = _uiState.value.items.firstOrNull { it.id == itemId } ?: return
        _uiState.update { state ->
            state.copy(
                items = state.items.map { i ->
                    if (i.id == itemId) {
                        i.copy(extraBarcodes = (i.extraBarcodes + barcode).distinct())
                    } else i
                }
            )
        }
        val updated = _uiState.value.items.first { it.id == itemId }
        _scanPopup.value = WearScanPopup.Recognized(barcode, updated)
    }

    private fun confirmPrevious() {
        val current = _scanPopup.value ?: return
        if (current is WearScanPopup.Recognized && !current.item.checked) {
            toggleItem(current.item.id)
            vibrate(80L)
        }
        _scanPopup.value = null
    }

    /** Botão Confirmar do popup: marca o item como comprado e fecha. */
    fun confirmScanPopup() {
        confirmPrevious()
    }

    /** Botão Preço do popup: define o preço do item escaneado (popup permanece aberto). */
    fun setScanItemPrice(price: Double?) {
        val current = _scanPopup.value
        if (current !is WearScanPopup.Recognized) return
        updateItemPrice(current.item.id, price)
        _scanPopup.value = WearScanPopup.Recognized(
            barcode = current.barcode,
            item = _uiState.value.items.first { it.id == current.item.id }
        )
    }

    /** Botão Qtd do popup: define a quantidade do item escaneado (popup permanece aberto). */
    fun setScanItemQuantity(quantity: Double) {
        val current = _scanPopup.value
        if (current !is WearScanPopup.Recognized) return
        updateItemQuantity(current.item.id, quantity)
        _scanPopup.value = WearScanPopup.Recognized(
            barcode = current.barcode,
            item = _uiState.value.items.first { it.id == current.item.id }
        )
    }

    fun dismissScanPopup() {
        _scanPopup.value = null
    }

    private fun vibrate(duration: Long) {
        val vibrator = context.getSystemService(Vibrator::class.java) ?: return
        vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
    }
}