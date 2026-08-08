package com.listaih.app.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.listaih.app.ui.theme.Theme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onListClick: (String, String) -> Unit,
    onAddListClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val lists = remember { mutableStateListOf<ShoppingListUi>(
        ShoppingListUi("1", "Compra Mensal", "shopping_cart", 3, 8, 47.50, true, listOf("JS", "MS", "P")),
        ShoppingListUi("2", "Churrasco Domingo", "celebration", 0, 8, 89.00, false, listOf("JS", "MS")),
        ShoppingListUi("3", "Farmácia", "medication", 0, 3, 12.00, false, listOf("JS"))
    ) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Listaih", fontSize = 20.sp, fontWeight = FontWeight.Medium) },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        floatingActionButton = {
            androidx.compose.material3.FloatingActionButton(
                onClick = onAddListClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add list")
            }
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.History, contentDescription = "Histórico") },
                    label = { Text("Histórico") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "Config") },
                    label = { Text("Config") },
                    selected = selectedTab == 2,
                    onClick = { onSettingsClick() }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(bottom = 80.dp), // Account for bottom nav
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Chips row
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(text = "Todas", selected = true, onClick = {})
                FilterChip(text = "Ativas", selected = false, onClick = {})
                FilterChip(text = "Arquivadas", selected = false, onClick = {})
                FilterChip(text = "Modelos", selected = false, onClick = {})
            }

            // Lists
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp)
            ) {
                items(lists) { list ->
                    ListCard(
                        list = list,
                        onClick = { onListClick(list.id, list.name) }
                    )
                }
            }
        }
    }
}

@Composable
fun FilterChip(text: String, selected: Boolean, onClick: () -> Unit) {
    androidx.compose.material3.Chip(
        onClick = onClick,
        selected = selected,
        modifier = Modifier.height(32.dp),
        colors = androidx.compose.material3.ChipDefaults.chipColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Text(text = text, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ListCard(list: ShoppingListUi, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = when (list.icon) {
                            "shopping_cart" -> androidx.compose.ui.res.R.drawable.ic_shopping_cart
                            "celebration" -> androidx.compose.ui.res.R.drawable.ic_celebration
                            "medication" -> androidx.compose.ui.res.R.drawable.ic_medication
                            else -> androidx.compose.ui.res.R.drawable.ic_launcher_foreground
                        }),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    androidx.compose.foundation.layout.Spacer(Modifier.width(8.dp))
                    Text(text = list.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = "${list.checkedItems}/${list.totalItems}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                        .background(MaterialTheme.colorScheme.primary, RectangleShape)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier.size(8.dp)
                        .background(
                            Color(
                                if (list.checkedItems == list.totalItems) 0xFF4CAF50
                                else if (list.checkedItems > 0) 0xFFFFC107
                                else 0xFF9E9E9E
                            ),
                            RectangleShape
                        )
                        .clip(RectangleShape)
                )
                androidx.compose.foundation.layout.Spacer(Modifier.width(6.dp))
                Text(
                    text = "Restam ${list.totalItems - list.checkedItems} itens · R$ ${String.format("%.2f", list.estimatedTotal)}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                list.members.forEachIndexed { index, initial ->
                    Avatar(
                        initial = initial,
                        isOnline = index < 2,
                        modifier = Modifier
                            .offset(x = (-index * 24).dp)
                    )
                }
            }
        }
    }
}

@Composable
fun Avatar(
    initial: String,
    isOnline: Boolean = false,
    modifier: Modifier = Modifier
) {
    val color = MaterialTheme.colorScheme.primary
    Box(
        modifier = modifier.size(36.dp),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .size(36.dp)
                .background(color, RectangleShape)
                .clip(RectangleShape)
        ) {
            Text(
                text = initial,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        if (isOnline) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(10.dp)
                    .background(Color(0xFF4CAF50), RectangleShape)
                    .clip(RectangleShape)
                    .border(2.dp, MaterialTheme.colorScheme.surface, RectangleShape)
            )
        }
    }
}

data class ShoppingListUi(
    val id: String,
    val name: String,
    val icon: String,
    val checkedItems: Int,
    val totalItems: Int,
    val estimatedTotal: Double,
    val hasOnlineMembers: Boolean,
    val members: List<String>
)