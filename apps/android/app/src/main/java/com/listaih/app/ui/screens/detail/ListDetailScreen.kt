package com.listaih.app.ui.screens.detail

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.listaih.app.ui.theme.Theme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.FloatingActionButton

@Composable
fun ListDetailScreen(
    listId: String,
    listName: String,
    onAddItemClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val items = remember { mutableStateListOf<ListItemUi>(
        ListItemUi("1", "Tomate", "2 kg", 8.00, null, "Hortifruti", false, null),
        ListItemUi("2", "Alface", "1 un", 3.00, 3.00, "Hortifruti", true, "Maria"),
        ListItemUi("3", "Cenoura", "500 g", 4.00, null, "Hortifruti", false, null),
        ListItemUi("4", "Leite", "2 un", 10.00, 10.00, "Laticínios", true, "João"),
        ListItemUi("5", "Queijo", "200 g", 12.00, null, "Laticínios", false, null),
        ListItemUi("6", "Pão", "1 un", 5.00, null, "Padaria", false, null),
        ListItemUi("7", "Arroz", "5 kg", 25.00, null, "Sem Categoria", false, null)
    ) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = listName, fontSize = 20.sp, fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Menu */ }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddItemClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add item")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(bottom = 80.dp)
        ) {
            // Members row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                MemberAvatars(members = listOf("JS", "MS", "P"), onlineCount = 2)
                androidx.compose.foundation.layout.Spacer(Modifier.width(8.dp))
                Text(text = "2 online", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Progress
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Itens (${items.size})", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "Comprados: ${items.count { it.checked }} ✓", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                androidx.compose.foundation.layout.Spacer(Modifier.height(4.dp))
                androidx.compose.material3.LinearProgressIndicator(
                    progress = if (items.isNotEmpty()) items.count { it.checked }.toFloat() / items.size else 0f,
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            // Items grouped by category
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp)
            ) {
                items(items.groupBy { it.category }.entries) { (category, categoryItems) ->
                    CategoryHeader(category = category, icon = getCategoryIcon(category))
                    items(categoryItems) { item ->
                        ListItemRow(
                            item = item,
                            onCheckClick = { /* TODO: Toggle check */ },
                            onClick = { /* TODO: Edit item */ }
                        )
                    }
                }
            }

            // Total row
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainer, RectangleShape)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    TotalRow(label = "Total estimado", value = "R$ ${String.format("%.2f", items.sumOf { it.estimatedPrice ?: 0.0 })}", bold = false)
                    TotalRow(label = "Total gasto", value = "R$ ${String.format("%.2f", items.filter { it.checked }.sumOf { it.actualPrice ?: 0.0 })}", bold = true, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun MemberAvatars(members: List<String>, onlineCount: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(-8.dp)) {
        members.forEachIndexed { index, initial ->
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(MaterialTheme.colorScheme.primary, RectangleShape)
                    .clip(RectangleShape)
                    .border(if (index > 0) 2.dp else 0.dp, MaterialTheme.colorScheme.surface, RectangleShape)
            ) {
                Text(text = initial, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun CategoryHeader(category: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        androidx.compose.foundation.layout.Spacer(Modifier.width(6.dp))
        Text(text = category.uppercase(), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ListItemRow(
    item: ListItemUi,
    onCheckClick: () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp, horizontal = 16.dp)
            .background(
                if (item.checked) MaterialTheme.colorScheme.surfaceContainer else Color.Transparent,
                RectangleShape
            )
            .clip(RectangleShape),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    if (item.checked) MaterialTheme.colorScheme.primary else Color.Transparent,
                    RectangleShape
                )
                .clip(RectangleShape)
                .border(if (!item.checked) 2.dp else 0.dp, MaterialTheme.colorScheme.outline, RectangleShape)
        ) {
            if (item.checked) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.align(Alignment.Center).size(16.dp))
            }
        }

        androidx.compose.foundation.layout.Spacer(Modifier.width(12.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = item.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = if (item.checked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                style = if (item.checked) androidx.compose.ui.text.TextStyle(textDecoration = androidx.compose.ui.text.TextDecoration.LineThrough) else null
            )
            Text(text = item.quantity, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Text(text = "R$ ${String.format("%.2f", item.estimatedPrice ?: 0.0)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)

        item.checkedBy?.let { by =>
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(text = "✓ por $by", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun TotalRow(label: String, value: String, bold: Boolean = false, color: Color = Color.Unspecified) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = if (bold) 16.sp else 14.sp, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal, color = color.takeOrElse { MaterialTheme.colorScheme.onSurface })
        Text(text = value, fontSize = if (bold) 16.sp else 14.sp, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal, color = color.takeOrElse { MaterialTheme.colorScheme.onSurface })
    }
}

fun getCategoryIcon(category: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (category.lowercase()) {
        "hortifruti", "frutas", "verduras", "legumes" -> androidx.compose.material.icons.filled.LocalGroceryStore
        "laticínios", "laticinios", "leite", "queijo" -> androidx.compose.material.icons.filled.LocalDrink
        "padaria", "pão", "pao" -> androidx.compose.material.icons.filled.BakeryDining
        "carnes", "açougue", "acougue" -> androidx.compose.material.icons.filled.SetMeal
        "peixaria", "peixe" -> androidx.compose.material.icons.filled.Seafood
        "bebidas" -> androidx.compose.material.icons.filled.LocalBar
        "limpeza" -> androidx.compose.material.icons.filled.CleaningServices
        "doces" -> androidx.compose.material.icons.filled.Cake
        "enlatados", "conservas" -> androidx.compose.material.icons.filled.Kitchen
        "mercearia" -> androidx.compose.material.icons.filled.ShoppingBasket
        "congelados" -> androidx.compose.material.icons.filled.AcUnit
        else -> androidx.compose.material.icons.filled.HelpOutline
    }
}

data class ListItemUi(
    val id: String,
    val name: String,
    val quantity: String,
    val estimatedPrice: Double?,
    val actualPrice: Double?,
    val category: String,
    val checked: Boolean,
    val checkedBy: String?
)