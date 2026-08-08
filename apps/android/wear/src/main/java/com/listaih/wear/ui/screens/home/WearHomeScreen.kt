package com.listaih.wear.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.listaih.wear.ui.theme.WearTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.List
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.ScalingLazyListState
import androidx.wear.compose.material.rememberScalingLazyListState
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Vignette

@Composable
fun WearHomeScreen(
    onListClick: (String, String) -> Unit,
    onSelectClick: () -> Unit,
    onVoiceClick: () -> Unit
) {
    val listState = rememberScalingLazyListState()
    val lists = remember { listOf(
        WearListUi("1", "Compra Mensal", 8, 3, 47.50),
        WearListUi("2", "Churrasco", 8, 0, 89.00),
        WearListUi("3", "Farmácia", 3, 0, 12.00)
    ) }

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 16.dp, bottom = 16.dp)
    ) {
        // App name
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Listaih", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                androidx.compose.foundation.layout.Spacer(Modifier.height(4.dp))
            }
        }

        // Progress ring for first list
        lists.firstOrNull()?.let { firstList ->
            item {
                WearProgressRing(
                    progress = if (firstList.totalItems > 0) firstList.checkedItems.toFloat() / firstList.totalItems else 0f,
                    listName = firstList.name,
                    remainingItems = firstList.totalItems - firstList.checkedItems
                )
            }
        }

        // Other lists
        lists.drop(1).forEach { list ->
            item {
                WearListCard(
                    list = list,
                    onClick = { onListClick(list.id, list.name) }
                )
            }
        }

        // Action buttons
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSelectClick,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    colors = androidx.wear.compose.material.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Filled.List, contentDescription = null, modifier = Modifier.size(20.dp))
                        androidx.compose.foundation.layout.Spacer(Modifier.width(8.dp))
                        Text(text = "Ver todas as listas", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }

                Button(
                    onClick = onVoiceClick,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    colors = androidx.wear.compose.material.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Filled.Mic, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        androidx.compose.foundation.layout.Spacer(Modifier.width(8.dp))
                        Text(text = "Adicionar por voz", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }
    }
}

@Composable
fun WearProgressRing(
    progress: Float,
    listName: String,
    remainingItems: Int
) {
    val circumference = 2 * 3.14159 * 52 // radius 52
    val strokeWidth = 8.dp
    val offset = circumference * (1 - progress)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.size(120.dp),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.size(120.dp)) {
                // Background circle
                drawCircle(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    radius = 52.dp.toPx(),
                    style = androidx.compose.ui.draw.Stroke(width = strokeWidth)
                )

                // Progress circle
                drawArc(
                    color = MaterialTheme.colorScheme.primary,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    radius = 52.dp.toPx(),
                    style = androidx.compose.ui.draw.Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "${(progress * 100).toInt()}%", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(text = listName, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Text(text = "$remainingItems restantes", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun WearListCard(list: WearListUi, onClick: () -> Unit) {
    Chip(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        label = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(text = list.name, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Text(text = "${list.totalItems - list.checkedItems} de ${list.totalItems} · R$ ${String.format("%.2f", list.estimatedTotal)}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

data class WearListUi(
    val id: String,
    val name: String,
    val totalItems: Int,
    val checkedItems: Int,
    val estimatedTotal: Double
)