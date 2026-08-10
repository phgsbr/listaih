package com.listaih.app.ui.scanpopup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Manages the post-scan popup state (ANDROID-PLAN Fase 6).
 *
 * Rules:
 * - Every scan opens a popup (6.1). No timer.
 * - A repeated scan of the same barcode adds +1 to the quantity of the
 *   displayed line (6.7).
 * - A scan of a different barcode auto-confirms the previous popup with the
 *   data it has (6.8), then opens the new one.
 * - Recognized popups stay discreet (screen stays off); unrecognized popups
 *   wake the screen (handled by the caller via [ScanPopupUi]).
 */
class ScanPopupController {

    var state by mutableStateOf<ScanPopupState?>(null)
        private set

    sealed interface ScanPopupState {
        /** Barcode matched an item already on the list. */
        data class Recognized(
            val item: ScanItem,
            /** Pending increments not yet applied to the backend. */
            val extra: Int = 0,
        ) : ScanPopupState

        /** Barcode not found on the list. */
        data class Unrecognized(val barcode: String) : ScanPopupState
    }

    data class ScanItem(
        val id: String,
        val name: String,
        val quantity: Double,
        val unit: String,
        val estimatedPrice: Double?,
        val category: String?,
        val barcode: String?,
    )

    sealed interface ScanDecision {
        /** Mark the item as bought with the full (accumulated) quantity. */
        data class Check(val itemId: String, val quantity: Double) : ScanDecision

        /** Increase the item quantity by one (repeated scan of same code). */
        data class Increment(val itemId: String, val quantity: Double) : ScanDecision

        /** Discard the previous popup (it had no actionable data). */
        data object Dismiss : ScanDecision
    }

    /**
     * Feeds a scanned barcode. Resolves the current popup (returns the action
     * to apply on the backend) and opens the new one.
     *
     * @param findItem maps a barcode to the current list item, or null.
     */
    fun handleBarcode(barcode: String, findItem: (String) -> ScanItem?): ScanDecision? {
        val previous = state
        val matched = findItem(barcode)

        val sameBarcode = previous is ScanPopupState.Recognized &&
            previous.item.barcode == barcode

        val decision: ScanDecision? = when (previous) {
            is ScanPopupState.Recognized -> {
                if (sameBarcode) {
                    val newQuantity = previous.item.quantity + previous.extra + 1
                    ScanDecision.Increment(previous.item.id, newQuantity)
                } else {
                    ScanDecision.Check(previous.item.id, previous.item.quantity + previous.extra)
                }
            }
            is ScanPopupState.Unrecognized -> ScanDecision.Dismiss
            null -> null
        }

        state = if (matched != null) {
            if (sameBarcode && previous is ScanPopupState.Recognized) {
                ScanPopupState.Recognized(matched, previous.extra + 1)
            } else {
                ScanPopupState.Recognized(matched)
            }
        } else {
            ScanPopupState.Unrecognized(barcode)
        }

        return decision
    }

    /** User tapped "Confirmar" on a recognized popup. */
    fun confirmCurrent(): ScanDecision? {
        val current = state as? ScanPopupState.Recognized ?: return null
        val decision = ScanDecision.Check(current.item.id, current.item.quantity + current.extra)
        state = null
        return decision
    }

    /** User dismissed the popup / finished an action. */
    fun clear() {
        state = null
    }
}