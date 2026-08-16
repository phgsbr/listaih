package com.listaih.wear.ui.screens.shopping

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.PositionIndicator
import com.listaih.wear.R
import com.listaih.wear.WearItemUi
import com.listaih.wear.WearMainViewModel

@Composable
fun WearShoppingScreen(
    viewModel: WearMainViewModel,
    listId: String,
    listName: String,
    onCheckout: (Int, Double) -> Unit,
    onBackClick: () -> Unit
) {
    val listState = rememberScalingLazyListState()
    val ui by viewModel.uiState.collectAsState()
    var editingItem by remember { mutableStateOf<WearItemUi?>(null) }

    val items = ui.items
    val checkedIds = items.filter { it.checked }.map { it.id }
    val checkedCount = checkedIds.size
    val totalCount = items.size
    val checkedTotal = items.filter { it.id in checkedIds }.sumOf { (it.estimatedPrice ?: 0.0) * it.quantity }
    val allChecked = checkedCount == totalCount && totalCount > 0

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = listName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
            val ringColor = if (allChecked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
            Box(contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(44.dp)) {
                    drawCircle(
                        color = trackColor,
                        radius = this.size.minDimension / 2
                    )
                    drawArc(
                        color = ringColor,
                        startAngle = -90f,
                        sweepAngle = 360f * (if (totalCount > 0) checkedCount.toFloat() / totalCount else 0f),
                        useCenter = false,
                        topLeft = Offset.Zero,
                        size = Size(this.size.width, this.size.height),
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Text(
                    text = "$checkedCount/$totalCount",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        ScalingLazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            state = listState,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 4.dp, bottom = 4.dp)
        ) {
            val openItems = items.filter { !it.checked }
            val boughtItems = items.filter { it.checked }

            openItems.groupBy { it.category }.forEach { (category, categoryItems) ->
                item {
                    WearCategoryHeader(category = category)
                }
                categoryItems.forEach { categoryItem ->
                    item {
                        WearItemRow(
                            item = categoryItem,
                            checked = false,
                            onClick = { viewModel.toggleItem(categoryItem.id) },
                            onLongClick = { editingItem = categoryItem }
                        )
                    }
                }
            }

            if (boughtItems.isNotEmpty()) {
                item {
                    WearCategoryHeader(category = stringResource(R.string.wear_shopping_bought))
                }
                boughtItems.forEach { boughtItem ->
                    item {
                        WearItemRow(
                            item = boughtItem,
                            checked = true,
                            onClick = { viewModel.toggleItem(boughtItem.id) },
                            onLongClick = { editingItem = boughtItem }
                        )
                    }
                }
            }
        }

        PositionIndicator(scalingLazyListState = listState)

        Button(
            onClick = { onCheckout(checkedCount, checkedTotal) },
            enabled = checkedCount > 0,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (allChecked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = if (allChecked) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onPrimary,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Text(
                text = if (checkedCount > 0) stringResource(R.string.wear_shopping_checkout, checkedTotal) else stringResource(R.string.wear_shopping_mark_items),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    editingItem?.let { item ->
        WearItemEditPopup(
            item = item,
            onDismiss = { editingItem = null },
            onConfirm = { quantity, price ->
                viewModel.updateItemQuantity(item.id, quantity)
                viewModel.updateItemPrice(item.id, price)
                editingItem = null
            }
        )
    }
}

@Composable
fun WearCategoryHeader(category: String) {
    val color = categoryColor(category)
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )
        Text(
            text = category.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(start = 6.dp)
        )
    }
}

@Composable
fun categoryColor(category: String): Color {
    val scheme = MaterialTheme.colorScheme
    return when (category.lowercase()) {
        "hortifruti" -> scheme.primary
        "laticínios", "laticinios" -> scheme.tertiary
        "padaria" -> Color(0xFFFFB74D)
        "carnes", "açougue", "acougue" -> Color(0xFFFF8A80)
        "bebidas" -> scheme.secondary
        "comprados", "purchased", "comprados" -> scheme.onSurfaceVariant
        else -> scheme.secondary
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun WearItemRow(
    item: WearItemUi,
    checked: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                if (checked) MaterialTheme.colorScheme.surfaceContainer
                else MaterialTheme.colorScheme.surfaceContainerHigh
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
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
                    color = if (checked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (checked) TextDecoration.LineThrough else null,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${formatQty(item.quantity)} ${item.unit}",
                    fontSize = 12.sp,
                    color = if (checked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item.estimatedPrice?.let { price ->
                Text(
                    text = "R$ ${String.format("%.2f", price * item.quantity)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (checked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.secondary
                )
            }
            if (checked) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = stringResource(R.string.cd_checked),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 6.dp).size(20.dp)
                )
            }
        }
    }
}

@Composable
fun WearItemEditPopup(
    item: WearItemUi,
    onDismiss: () -> Unit,
    onConfirm: (Double, Double?) -> Unit
) {
    var quantity by remember { mutableStateOf(item.quantity) }
    var price by remember { mutableStateOf(item.estimatedPrice?.toString() ?: "") }
    val priceValue = price.toDoubleOrNull()
    val total = priceValue?.times(quantity) ?: 0.0

    BackHandler(onBack = onDismiss)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.9f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = item.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.category.uppercase(),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = categoryColor(item.category),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = stringResource(R.string.wear_shopping_quantity, item.unit),
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(top = 1.dp)
            ) {
                StepButton(onClick = { quantity = (quantity - 0.5).coerceAtLeast(0.5) }, enabled = quantity > 0.5) {
                    Icon(Icons.Filled.Remove, contentDescription = stringResource(R.string.cd_decrease), tint = MaterialTheme.colorScheme.onPrimary)
                }
                Text(
                    text = formatQty(quantity),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    minLines = 1
                )
                StepButton(onClick = { quantity = quantity + 0.5 }) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.cd_increase), tint = MaterialTheme.colorScheme.onPrimary)
                }
            }

            OutlinedTextField(
                value = price,
                onValueChange = { new ->
                    price = new.filter { c -> c.isDigit() || c == '.' }.take(10)
                },
                label = { Text(stringResource(R.string.wear_shopping_price_label), fontSize = 11.sp) },
                placeholder = { Text(stringResource(R.string.wear_shopping_price_placeholder)) },
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .padding(top = 2.dp)
            )

            Text(
                text = stringResource(R.string.wear_shopping_total, total),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(top = 3.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(44.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text(text = stringResource(R.string.btn_cancel), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
                Button(
                    onClick = { onConfirm(quantity, priceValue) },
                    enabled = priceValue != null && priceValue > 0,
                    modifier = Modifier.weight(1f).height(44.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text(text = stringResource(R.string.btn_save), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun StepButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(
                if (enabled) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceContainerHighest
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

private fun formatQty(qty: Double): String {
    return if (qty == qty.toLong().toDouble()) {
        qty.toLong().toString()
    } else {
        String.format("%.2f", qty).trimEnd('0').trimEnd('.')
    }
}