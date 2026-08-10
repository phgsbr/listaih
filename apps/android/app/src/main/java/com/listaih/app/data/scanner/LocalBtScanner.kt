package com.listaih.app.data.scanner

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * CompositionLocal exposing the BtScannerManager provided by MainActivity,
 * so screens can register their barcode handler while visible.
 */
val LocalBtScanner = staticCompositionLocalOf<BtScannerManager?> { null }