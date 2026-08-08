package com.listaih.wear.ui.screens.select

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.listaih.wear.ui.theme.WearTheme
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.ScalingLazyListState
import androidx.wear.compose.material.rememberScalingLazyListState
import androidx.wear.compose.material.Vignette

@Composable
fun WearSelectScreen(
    onListClick: (String, String) -> Unit,
    onBackClick: () -> Unit
) {
    val listState = rememberScalingLazyListState()
    val lists = remember { listOf(
        WearListUi("1", "Compra Mensal", 8, 3, 47.50),
        WearListUi("2", "Churrasco", 8, 0, 89.00),
        WearListUi("3", "Farmácia", 3, 0, 12.00),
        WearListUi("4", "Material de Construção", 5, 2, 150.00)
    ) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Vignette(
                vignettePosition = androidx.wear.compose.material.VignettePosition.Top,
                content = {
                    Text(text = "Minhas Listas", fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
                }
            )
        }

        // Lists
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 8.dp, bottom = 16.dp)
        ) {
            items(lists) { list ->
                item {
                    WearSelectListCard(
                        list = list,
                        onClick = { onListClick(list.id, list.name) }
                    )
                }
            }
        }
    }
}

@Composable
fun WearSelectListCard(list: WearListUi, onClick: () -> Unit) {
    Chip(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        label = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = list.name, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    Chip(
                        onClick = { /* noop */ },
                        label = {
                            Text(text = "${list.totalItems - list.checkedItems}/${list.totalItems}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        },
                        colors = androidx.wear.compose.material.ChipDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        },
        colors = androidx.wear.compose.material.ChipDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    )
}

data class WearListUi(
    val id: String,
    val name: String,
    val totalItems: Int,
    val checkedItems: Int,
    val estimatedTotal: Double
)