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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.QrScanner
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Check

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

    LaunchedEffect(key1 = uiState.checkoutSuccess) {
        if (uiState.checkoutSuccess) {
            scope.launch {
                val result = snackbarHostState.showSnackbar(
                    "Compra finalizada com sucesso!",
                    duration = 3000
                )
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
                snackbarHostState.showSnackbar(error, duration = 3000)
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
                            text = "${uiState.checkedCount}/${uiState.totalCount} itens",
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
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
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
                    Icon(Icons.Filled.Payment, contentDescription = "Checkout")
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
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
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
                            TextButton(onClick = { /* TODO: Scanner */ }) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Filled.QrScanner,
                                        contentDescription = "Scanner",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text("Escanear", fontSize = 13.sp)
                                }
                            }
                            TextButton(onClick = { /* TODO: Clear checked */ }) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "Clear",
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
                colors = androidx.compose.material3.CheckboxDefaults.checkboxColors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedBorderColor = MaterialTheme.colorScheme.outline
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
                        androidx.compose.ui.text.TextStyle(
                            textDecoration = androidx.compose.ui.text.TextDecoration.LineThrough
                        )
                    } else null
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

import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.res.painterResource

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

    val paymentOptions = listOf(
        "" to "Sem pagamento" to 0,
        "DINHEIRO" to "Dinheiro" to R.drawable.payment_dinheiro,
        "DEBITO" to "Débito" to R.drawable.payment_debito,
        "CREDITO" to "Crédito" to R.drawable.payment_credito,
        "PIX" to "PIX" to R.drawable.payment_pix,
        "VR" to "VR" to R.drawable.payment_vr,
        "VA" to "VA" to R.drawable.payment_va
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Finalizar compra") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "${uiState.checkedCount} itens marcados",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = paymentOptions.find { it.first == paymentMethod }?.second ?: "Sem pagamento",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Forma de pagamento") },
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
                            androidx.compose.material.icons.filled.ArrowDropDown,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.menuAnchor()
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
                OutlinedTextField(
                    value = totalAmount,
                    onValueChange = { totalAmount = it },
                    label = { Text("Total (R$)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Observações") },
                    maxLines = 3
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Sincronizar com Grocy", fontSize = 14.sp)
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
                    Text("Confirmar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
