package com.listaih.wear.ui.screens.shopping

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.listaih.wear.ui.theme.WearTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.ScalingLazyListState
import androidx.wear.compose.material.rememberScalingLazyListState
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Vignette

@Composable
fun WearShoppingScreen(
    listId: String,
    listName: String,
    onItemCheck: (String) -> Unit,
    onComplete: () -> Unit,
    onBackClick: () -> Unit
) {
    val listState = rememberScalingLazyListState()
    val items = remember { listOf(
        WearItemUi("1", "Tomate", "2 kg", false, "Hortifruti"),
        WearItemUi("2", "Alface", "1 un", true, "Hortifruti"),
        WearItemUi("3", "Cenoura", "500 g", false, "Hortifruti"),
        WearItemUi("4", "Leite", "2 un", true, "Laticínios"),
        WearItemUi("5", "Queijo", "200 g", false, "Laticínios"),
        WearItemUi("6", "Pão", "1 un", false, "Padaria"),
        WearItemUi("7", "Arroz", "5 kg", false, "Sem Categoria")
    ) }

    val checkedCount = items.count { it.checked }
    val totalCount = items.size

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header with progress
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Vignette(
                vignettePosition = androidx.wear.compose.material.VignettePosition.Top,
                content = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = listName, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        androidx.compose.foundation.layout.Spacer(Modifier.height(4.dp))
                        androidx.wear.compose.material.CircularProgressIndicator(
                            progress = if (totalCount > 0) checkedCount.toFloat() / totalCount else 0f,
                            modifier = Modifier.size(48.dp),
                            strokeWidth = 4.dp,
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(text = "$checkedCount/$totalCount", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            )
        }

        // Items
        ScalingLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 60.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 8.dp, bottom = 16.dp)
            ) {
                items(items.groupBy { it.category }.entries) { (category, categoryItems) ->
                    item {
                        WearCategoryHeader(category = category)
                    }
                    categoryItems.forEach { item ->
                        item {
                            WearItemRow(
                                item = item,
                                onClick = { onItemCheck(item.id) }
                            )
                        }
                    }
                }
            }
        }

        // Complete button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .align(Alignment.BottomCenter)
        ) {
            Button(
                onClick = onComplete,
                enabled = checkedCount == totalCount && totalCount > 0,
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.wear.compose.material.ButtonDefaults.buttonColors(
                    containerColor = if (checkedCount == totalCount) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer
                )
            ) {
                Text(text = if (checkedCount == totalCount) "Finalizar compra" : "Marcar itens para finalizar", fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun WearCategoryHeader(category: String) {
    Chip(
        onClick = { /* TODO: Expand/collapse */ },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        label = {
            Text(text = category.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        colors = androidx.wear.compose.material.ChipDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    )
}

@Composable
fun WearItemRow(
    item: WearItemUi,
    onClick: () -> Unit
) {
    Chip(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        label = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = item.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (item.checked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                        style = if (item.checked) androidx.compose.ui.text.TextStyle(textDecoration = androidx.compose.ui.text.TextDecoration.LineThrough) else null
                    )
                    Text(text = item.quantity, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                if (item.checked) {
                    Icon(Icons.Filled.Check, contentDescription = "Checked", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                }
            }
        },
        colors = androidx.wear.compose.material.ChipDefaults.colors(
            containerColor = if (item.checked) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surface
        )
    )
}

data class WearItemUi(
    val id: String,
    val name: String,
    val quantity: String,
    val checked: Boolean,
    val category: String
)