package com.listaih.app.ui.scanpopup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.listaih.app.R
import com.listaih.app.ui.scanpopup.ScanPopupController.ScanPopupState

/** Item entry shown in the "Associar à lista" chooser. */
data class AssociableItem(
    val id: String,
    val name: String,
    val category: String?,
)

/**
 * Renders the post-scan popup (Fase 6):
 * - Recognized: discreet card, does NOT wake the screen (6.2).
 * - Unrecognized: dialog with 3 actions, the caller wakes the screen (6.3).
 */
@Composable
fun ScanPopupHost(
    state: ScanPopupState?,
    items: List<AssociableItem>,
    busy: Boolean,
    onConfirmRecognized: () -> Unit,
    onDismiss: () -> Unit,
    onAssociate: (String) -> Unit,
    onCreateNew: (String) -> Unit,
    onCreateGeneric: () -> Unit,
    onSuggestName: suspend (String) -> String?,
) {
    var showAssociate by remember { mutableStateOf(false) }
    var showNewProduct by remember { mutableStateOf(false) }

    LaunchedEffect(state) {
        if (state == null) {
            showAssociate = false
            showNewProduct = false
        }
    }

    when (val current = state) {
        is ScanPopupState.Recognized -> {
            val quantity = current.item.quantity + current.extra
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp)
                        ) {
                            Text(
                                text = current.item.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${formatQty(quantity)} ${current.item.unit}" +
                                    (current.item.estimatedPrice?.let {
                                        " · R$ ${String.format("%.2f", it * quantity)}"
                                    } ?: ""),
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Filled.Close, contentDescription = "Fechar")
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = onConfirmRecognized,
                            enabled = !busy
                        ) {
                            if (busy) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp))
                            } else {
                                Text("Confirmar", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        is ScanPopupState.Unrecognized -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text("Código não reconhecido") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = current.barcode,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Este código não está na lista. O que fazer?",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                confirmButton = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        TextButton(
                            onClick = { showAssociate = true },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !busy
                        ) { Text("Associar à lista") }
                        TextButton(
                            onClick = { showNewProduct = true },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !busy
                        ) { Text("Cadastrar novo") }
                        TextButton(
                            onClick = onCreateGeneric,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !busy
                        ) { Text("Genérico") }
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss, enabled = !busy) { Text("Fechar") }
                }
            )
        }

        null -> Unit
    }

    if (showAssociate) {
        AssociateItemDialog(
            items = items,
            onSelect = onAssociate,
            onDismiss = { showAssociate = false }
        )
    }

    if (showNewProduct) {
        val barcode = (state as? ScanPopupState.Unrecognized)?.barcode ?: ""
        NewProductDialog(
            barcode = barcode,
            busy = busy,
            onSave = onCreateNew,
            onDismiss = { showNewProduct = false },
            onSuggestName = onSuggestName,
        )
    }
}

@Composable
private fun AssociateItemDialog(
    items: List<AssociableItem>,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Associar a um item da lista") },
        text = {
            if (items.isEmpty()) {
                Text(
                    text = "Nenhum item na lista.",
                    fontSize = 14.sp
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(items, key = { it.id }) { item ->
                        TextButton(
                            onClick = { onSelect(item.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = item.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = item.category ?: "Sem categoria",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Fechar") }
        }
    )
}

@Composable
private fun NewProductDialog(
    barcode: String,
    busy: Boolean,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit,
    onSuggestName: suspend (String) -> String?,
) {
    var name by remember { mutableStateOf("") }

    LaunchedEffect(barcode) {
        if (name.isBlank()) {
            name = onSuggestName(barcode) ?: ""
        }
    }

    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        title = { Text("Cadastrar novo produto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome do produto") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Código: $barcode",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name) },
                enabled = !busy && name.isNotBlank()
            ) {
                if (busy) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                } else {
                    Text("Salvar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !busy) { Text("Cancelar") }
        }
    )
}

private fun formatQty(qty: Double): String {
    return if (qty == qty.toLong().toDouble()) {
        qty.toLong().toString()
    } else {
        String.format("%.2f", qty).trimEnd('0').trimEnd('.')
    }
}