package com.listaih.app.ui.screens.shopping

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ArrowDropDownCircle
import androidx.compose.ui.platform.LocalContext
import com.listaih.app.R
import com.listaih.app.data.scanner.HapticFeedback
import com.listaih.app.data.scanner.LocalBtScanner
import com.listaih.app.data.scanner.ScreenWake
import com.listaih.app.ui.scanpopup.AssociableItem
import com.listaih.app.ui.scanpopup.ScanPopupController
import com.listaih.app.ui.scanpopup.ScanPopupHost

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingModeScreen(
    listId: String,
    listName: String,
    onBackClick: () -> Unit,
    onCheckoutComplete: () -> Unit
) {
    val viewModel: ShoppingModeViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showScanner by rememberSaveable { mutableStateOf(false) }

    val btScanner = LocalBtScanner.current
    val scanController = remember { ScanPopupController() }

    fun findScanItem(barcode: String): ScanPopupController.ScanItem? {
        return uiState.items.firstOrNull { it.barcode == barcode }?.let {
            ScanPopupController.ScanItem(
                id = it.id,
                name = it.name,
                quantity = it.quantity,
                unit = it.unit,
                estimatedPrice = it.estimatedPrice,
                category = it.category,
                barcode = it.barcode,
            )
        }
    }

    fun handleBarcode(barcode: String) {
        when (val decision = scanController.handleBarcode(barcode, ::findScanItem)) {
            is ScanPopupController.ScanDecision.Check ->
                viewModel.confirmScannedItem(decision.itemId, decision.quantity)
            is ScanPopupController.ScanDecision.Increment ->
                viewModel.incrementScannedItem(decision.itemId, decision.quantity)
            is ScanPopupController.ScanDecision.Dismiss -> Unit
            null -> Unit
        }
        when (scanController.state) {
            is ScanPopupController.ScanPopupState.Unrecognized -> {
                HapticFeedback.error(context)
                ScreenWake.wake(context)
            }
            is ScanPopupController.ScanPopupState.Recognized -> {
                HapticFeedback.success(context)
                ScreenWake.clear(context)
            }
            null -> ScreenWake.clear(context)
        }
        showScanner = false
    }

    val checkoutSuccessText = stringResource(R.string.shopping_checkout_success)

    val currentHandleBarcode by androidx.compose.runtime.rememberUpdatedState(::handleBarcode)

    DisposableEffect(btScanner) {
        btScanner?.onBarcodeScanned = { barcode -> currentHandleBarcode(barcode) }
        onDispose {
            btScanner?.onBarcodeScanned = null
            ScreenWake.clear(context)
        }
    }

    LaunchedEffect(key1 = uiState.checkoutSuccess) {
        if (uiState.checkoutSuccess) {
            scope.launch {
                val result = snackbarHostState.showSnackbar(checkoutSuccessText)
                if (result == SnackbarResult.ActionPerformed) {
                    onCheckoutComplete()
                }
            }
            onCheckoutComplete()
            viewModel.clearCheckoutSuccess()
        }
    }

    LaunchedEffect(key1 = uiState.checkoutError) {
        uiState.checkoutError?.let { error ->
            scope.launch {
                snackbarHostState.showSnackbar(error)
            }
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = listName, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${uiState.checkedCount}/${uiState.totalCount} ${stringResource(R.string.shopping_checkout_items_count, uiState.checkedCount).split(" ").lastOrNull() ?: ""}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.showPaymentDialog) {
                            viewModel.dismissPaymentDialog()
                        } else {
                            onBackClick()
                        }
                    }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.shopping_back_cd))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        floatingActionButton = {
            if (uiState.checkedCount > 0 && !uiState.isLoading) {
                FloatingActionButton(
                    onClick = { viewModel.openPaymentDialog() },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Filled.Payment, contentDescription = stringResource(R.string.shopping_checkout_cd))
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.isLoading && uiState.items.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        top = 8.dp, bottom = 88.dp
                    )
                ) {
                    items(uiState.items, key = { it.id }) { item ->
                        ShoppingItemRow(
                            item = item,
                            onToggleCheck = { checked ->
                                viewModel.toggleCheck(item.id, item.checked)
                            },
                            onTap = {
                                // Tap to edit (placeholder - could open edit dialog)
                            }
                        )
                    }
                }

                // Bottom bar with total and scan button
                BottomAppBar(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total: R$ ${String.format("%.2f", uiState.estimatedTotal)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { showScanner = true }) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Filled.QrCodeScanner,
                                        contentDescription = stringResource(R.string.shopping_scanner_cd),
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(stringResource(R.string.shopping_scan_button), fontSize = 13.sp)
                                }
                            }
                            TextButton(onClick = { viewModel.uncheckAll() }) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = stringResource(R.string.shopping_clear_cd),
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (uiState.showPaymentDialog) {
                CheckoutDialog(
                    viewModel = viewModel,
                    uiState = uiState,
                    onDismiss = { viewModel.dismissPaymentDialog() },
                    onConfirm = { viewModel.doCheckout() }
                )
            }

            if (scanController.state != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp)
                ) {
                    ScanPopupHost(
                        state = scanController.state,
                        items = uiState.items.map {
                            AssociableItem(id = it.id, name = it.name, category = it.category)
                        },
                        busy = uiState.isScanBusy,
                        onConfirmRecognized = {
                            scanController.confirmCurrent()?.let { decision ->
                                if (decision is ScanPopupController.ScanDecision.Check) {
                                    viewModel.confirmScannedItem(decision.itemId, decision.quantity)
                                }
                            }
                            ScreenWake.clear(context)
                        },
                        onDismiss = {
                            scanController.clear()
                            ScreenWake.clear(context)
                        },
                        onAssociate = { itemId ->
                            val barcode = (scanController.state as? ScanPopupController.ScanPopupState.Unrecognized)?.barcode
                            if (barcode != null) {
                                viewModel.associateScannedItem(itemId, barcode)
                            }
                            scanController.clear()
                            ScreenWake.clear(context)
                        },
                        onCreateNew = { name ->
                            val barcode = (scanController.state as? ScanPopupController.ScanPopupState.Unrecognized)?.barcode
                            if (barcode != null) {
                                viewModel.createScannedProduct(barcode, name)
                            }
                            scanController.clear()
                            ScreenWake.clear(context)
                        },
                        onCreateGeneric = {
                            val barcode = (scanController.state as? ScanPopupController.ScanPopupState.Unrecognized)?.barcode
                            if (barcode != null) {
                                viewModel.createGenericScannedProduct(barcode)
                            }
                            scanController.clear()
                            ScreenWake.clear(context)
                        },
                        onSuggestName = { barcode -> viewModel.suggestScannedName(barcode) }
                    )
                }
            }

            if (showScanner) {
                BarcodeScannerScreen(
                    onBackClick = { showScanner = false },
                    onBarcodeScanned = ::handleBarcode
                )
            }
        }
    }
}

@Composable
fun ShoppingItemRow(
    item: ShoppingItemUi,
    onToggleCheck: (Boolean) -> Unit,
    onTap: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onTap() }
                )
            },
        colors = CardDefaults.cardColors(
            containerColor = if (item.checked) {
                MaterialTheme.colorScheme.surfaceContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.checked,
                onCheckedChange = { onToggleCheck(it) },
                colors = androidx.compose.material3.CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.outline
                )
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = item.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (item.checked) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    style = if (item.checked) {
                        MaterialTheme.typography.bodyLarge.copy(
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                        )
                    } else {
                        MaterialTheme.typography.bodyLarge
                    }
                )
                Text(
                    text = "${formatQty(item.quantity)} ${item.unit}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item.estimatedPrice?.let { price ->
                Text(
                    text = "R$ ${String.format("%.2f", price * item.quantity)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (item.checked) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.secondary
                    }
                )
            }
        }
    }
}

private fun formatQty(qty: Double): String {
    return if (qty == qty.toLong().toDouble()) {
        qty.toLong().toString()
    } else {
        String.format("%.2f", qty).trimEnd('0').trimEnd('.')
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutDialog(
    viewModel: ShoppingModeViewModel,
    uiState: ShoppingUiState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var paymentMethod by remember { mutableStateOf(uiState.paymentMethod ?: "") }
    var totalAmount by remember { mutableStateOf(uiState.totalAmount?.toString() ?: "") }
    var notes by remember { mutableStateOf(uiState.notes ?: "") }
    var grocySync by remember { mutableStateOf(uiState.syncingToGrocy) }
    var paymentDropdownExpanded by remember { mutableStateOf(false) }

    val paymentNoneText = stringResource(R.string.common_payment_none)
    val paymentCashText = stringResource(R.string.common_payment_cash)
    val paymentDebitText = stringResource(R.string.common_payment_debit)
    val paymentCreditText = stringResource(R.string.common_payment_credit)
    val paymentPixText = stringResource(R.string.common_payment_pix)
    val paymentVrText = stringResource(R.string.common_payment_vr)
    val paymentVaText = stringResource(R.string.common_payment_va)

    val paymentOptions = listOf(
        Triple("", paymentNoneText, 0),
        Triple("DINHEIRO", paymentCashText, R.drawable.payment_dinheiro),
        Triple("DEBITO", paymentDebitText, R.drawable.payment_debito),
        Triple("CREDITO", paymentCreditText, R.drawable.payment_credito),
        Triple("PIX", paymentPixText, R.drawable.payment_pix),
        Triple("VR", paymentVrText, R.drawable.payment_vr),
        Triple("VA", paymentVaText, R.drawable.payment_va)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.shopping_checkout_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.shopping_checkout_items_count, uiState.checkedCount),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                androidx.compose.material3.ExposedDropdownMenuBox(
                    expanded = paymentDropdownExpanded,
                    onExpandedChange = { paymentDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = paymentOptions.find { it.first == paymentMethod }?.second ?: paymentNoneText,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.shopping_checkout_payment_label)) },
                        leadingIcon = {
                            val selected = paymentOptions.find { it.first == paymentMethod }
                            if (selected != null && selected.third != 0) {
                                Icon(
                                    painter = painterResource(selected.third),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        trailingIcon = {
                            androidx.compose.material3.Icon(
                                Icons.Filled.ArrowDropDownCircle,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    androidx.compose.material3.DropdownMenu(
                        expanded = paymentDropdownExpanded,
                        onDismissRequest = { paymentDropdownExpanded = false }
                    ) {
                        paymentOptions.forEach { (value, label, drawableRes) ->
                            androidx.compose.material3.DropdownMenuItem(
                                leadingIcon = {
                                    if (drawableRes != 0) {
                                        Icon(
                                            painter = painterResource(drawableRes),
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                },
                                text = { Text(label) },
                                onClick = {
                                    paymentMethod = value
                                    paymentDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = totalAmount,
                    onValueChange = { totalAmount = it },
                    label = { Text(stringResource(R.string.shopping_checkout_total_label)) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(stringResource(R.string.shopping_checkout_notes_label)) },
                    maxLines = 3
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(R.string.shopping_checkout_grocy_sync), fontSize = 14.sp)
                    androidx.compose.material3.Switch(
                        checked = grocySync,
                        onCheckedChange = { grocySync = it }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    viewModel.setPaymentMethod(if (paymentMethod.isEmpty()) null else paymentMethod)
                    viewModel.setTotalAmount(if (totalAmount.isEmpty()) null else totalAmount.toDoubleOrNull())
                    viewModel.setNotes(if (notes.isEmpty()) null else notes)
                    viewModel.setGrocySync(grocySync)
                    onConfirm()
                },
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                } else {
                    Text(stringResource(R.string.shopping_checkout_confirm))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}
