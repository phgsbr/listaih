package com.listaih.wear.ui.scanpopup

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.items
import com.listaih.wear.WearItemUi
import com.listaih.wear.WearMainViewModel
import com.listaih.wear.WearScanPopup
import com.listaih.wear.ui.screens.shopping.categoryColor
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun WearScanPopupHost(viewModel: WearMainViewModel) {
    val popup by viewModel.scanPopup.collectAsState()
    val key = (popup as? WearScanPopup.Recognized)?.barcode
    var editing by remember(key) { mutableStateOf<EditorMode?>(null) }

    BackHandler(enabled = popup != null) {
        viewModel.dismissScanPopup()
    }

    when (val current = popup) {
        is WearScanPopup.Recognized -> {
            when (editing) {
                EditorMode.QTY -> ScanKeypadEditor(
                    item = current.item,
                    mode = EditorMode.QTY,
                    onBack = { editing = null },
                    onSave = { value ->
                        viewModel.setScanItemQuantity(value)
                        editing = null
                    }
                )
                EditorMode.PRICE -> ScanKeypadEditor(
                    item = current.item,
                    mode = EditorMode.PRICE,
                    onBack = { editing = null },
                    onSave = { value ->
                        viewModel.setScanItemPrice(value)
                        editing = null
                    }
                )
                null -> RecognizedScanPopup(
                    item = current.item,
                    onQty = { editing = EditorMode.QTY },
                    onPrice = { editing = EditorMode.PRICE },
                    onConfirm = { viewModel.confirmScanPopup() },
                    onDismiss = { viewModel.dismissScanPopup() }
                )
            }
        }
        is WearScanPopup.Unrecognized -> UnrecognizedFlow(
            barcode = current.barcode,
            items = viewModel.uiState.collectAsState().value.items,
            onAssociate = { itemId -> viewModel.associateBarcode(itemId, current.barcode) },
            onDismiss = { viewModel.dismissScanPopup() }
        )
        null -> Unit
    }
}

private enum class EditorMode { QTY, PRICE }

/** Overlay padrão do popup: fundo escuro + swipe lateral para fechar. */
@Composable
private fun ScanPopupContainer(
    onSwipeClose: () -> Unit,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.9f))
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        if (dragAmount > 0f) {
                            scope.launch { offsetX.snapTo((offsetX.value + dragAmount).coerceAtLeast(0f)) }
                        }
                    },
                    onDragEnd = {
                        if (offsetX.value > 180f) {
                            onSwipeClose()
                        } else {
                            scope.launch { offsetX.animateTo(0f) }
                        }
                    },
                    onDragCancel = {
                        scope.launch { offsetX.animateTo(0f) }
                    }
                )
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) },
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@Composable
private fun RecognizedScanPopup(
    item: WearItemUi,
    onQty: () -> Unit,
    onPrice: () -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    ScanPopupContainer(onSwipeClose = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${formatQty(item.quantity)} ${item.unit}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    item.estimatedPrice?.let { price ->
                        Text(
                            text = "R$ ${String.format("%.2f", price * item.quantity)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
            Text(
                text = "escaneie de novo p/ +1",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PopupButton(
                    icon = Icons.Filled.Add,
                    contentDescription = "Definir quantidade",
                    onClick = onQty
                )
                PopupButton(
                    icon = Icons.Filled.AttachMoney,
                    contentDescription = "Definir preço",
                    onClick = onPrice
                )
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f).height(36.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2E7D32),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = "Confirmar",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.PopupButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.weight(1f).height(36.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun UnrecognizedFlow(
    barcode: String,
    items: List<WearItemUi>,
    onAssociate: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var showPicker by remember(barcode) { mutableStateOf(false) }
    var showPhone by remember(barcode) { mutableStateOf(false) }

    when {
        showPhone -> CreateOnPhonePopup(
            barcode = barcode,
            onClose = { showPhone = false },
            onDismiss = onDismiss
        )
        showPicker -> AssociatePicker(
            barcode = barcode,
            items = items,
            onPick = onAssociate,
            onBack = { showPicker = false },
            onDismiss = onDismiss
        )
        else -> UnrecognizedScanPopup(
            barcode = barcode,
            onAssociate = { showPicker = true },
            onCreateNew = { showPhone = true },
            onDismiss = onDismiss
        )
    }
}

@Composable
private fun UnrecognizedScanPopup(
    barcode: String,
    onAssociate: () -> Unit,
    onCreateNew: () -> Unit,
    onDismiss: () -> Unit
) {
    ScanPopupContainer(onSwipeClose = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Filled.Close,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = "Código não encontrado",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = barcode,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Button(
                onClick = onAssociate,
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp).height(34.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(text = "Associar a um produto", fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
            Button(
                onClick = onCreateNew,
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp).height(34.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(text = "Criar novo (no celular)", fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun AssociatePicker(
    barcode: String,
    items: List<WearItemUi>,
    onPick: (String) -> Unit,
    onBack: () -> Unit,
    onDismiss: () -> Unit
) {
    BackHandler(onBack = onBack)

    ScanPopupContainer(onSwipeClose = onBack) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Associar $barcode",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "escolha o produto",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            ScalingLazyColumn(
                modifier = Modifier.fillMaxWidth().height(150.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 2.dp)
            ) {
                items(items.size) { index ->
                    val it = items[index]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                            .clickable { onPick(it.id) }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = it.name,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${formatQty(it.quantity)} ${it.unit}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().height(30.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(text = "Voltar", fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun CreateOnPhonePopup(
    barcode: String,
    onClose: () -> Unit,
    onDismiss: () -> Unit
) {
    ScanPopupContainer(onSwipeClose = onClose) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Filled.PhoneAndroid,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(26.dp)
            )
            Text(
                text = "Cadastre no celular",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Crie o produto no app do celular — ele aparecerá no relógio.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Text(
                text = barcode,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onClose,
                    modifier = Modifier.weight(1f).height(32.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text(text = "Voltar", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(32.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(text = "Fechar", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun ScanKeypadEditor(
    item: WearItemUi,
    mode: EditorMode,
    onBack: () -> Unit,
    onSave: (Double) -> Unit
) {
    val fractionalQty = item.unit.equals("kg", true) || item.unit.equals("l", true)
    var digits by remember(item.id) { mutableStateOf(0L) }
    val value = when (mode) {
        EditorMode.PRICE -> digits / 100.0
        EditorMode.QTY -> if (fractionalQty) digits / 100.0 else digits.toDouble()
    }
    val total = when (mode) {
        EditorMode.PRICE -> value * item.quantity
        EditorMode.QTY -> value * (item.estimatedPrice ?: 0.0)
    }
    val displayText = when (mode) {
        EditorMode.PRICE -> formatBrl(value)
        EditorMode.QTY -> "${formatQty(value)} ${item.unit}"
    }
    val hint = when (mode) {
        EditorMode.PRICE -> "digite só os números · 1299 = R$ 12,99"
        EditorMode.QTY -> if (fractionalQty) {
            "digite só os números · 250 = 2,50 ${item.unit}"
        } else {
            "digite só os números · 2 = 2 ${item.unit}"
        }
    }

    BackHandler(onBack = onBack)

    ScanPopupContainer(onSwipeClose = onBack) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = hint,
                    fontSize = 8.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = displayText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 1.dp)
            )
            Text(
                text = "Total: ${formatBrl(total)}",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )

            Column(
                modifier = Modifier.padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0").chunked(5).forEach { rowKeys ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        rowKeys.forEach { digit ->
                            KeypadKey(
                                text = digit,
                                onClick = {
                                    digits = (digits * 10 + digit.toLong()).coerceAtMost(99_999_999L)
                                },
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    KeypadKey(text = "⌫", onClick = { digits /= 10L }, modifier = Modifier.size(46.dp, 30.dp))
                    KeypadKey(text = "C", onClick = { digits = 0L }, modifier = Modifier.size(46.dp, 30.dp))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onBack,
                    modifier = Modifier.weight(1f).height(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text(text = "Voltar", fontSize = 10.sp, fontWeight = FontWeight.Medium)
                }
                Button(
                    onClick = { onSave(value) },
                    enabled = digits > 0,
                    modifier = Modifier.weight(1f).height(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text(text = "Salvar", fontSize = 10.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun KeypadKey(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun formatQty(qty: Double): String {
    return if (qty == qty.toLong().toDouble()) {
        qty.toLong().toString()
    } else {
        String.format("%.2f", qty).trimEnd('0').trimEnd('.').replace('.', ',')
    }
}

/** Formata valor em reais com vírgula (pt-BR): 12.99 → "R$ 12,99" */
private fun formatBrl(value: Double): String {
    val cents = Math.round(value * 100.0)
    val reais = cents / 100
    val centavos = Math.abs(cents % 100).toString().padStart(2, '0')
    return "R$ $reais,$centavos"
}