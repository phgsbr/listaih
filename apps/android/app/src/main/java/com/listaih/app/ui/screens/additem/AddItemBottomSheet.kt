package com.listaih.app.ui.screens.additem

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.listaih.app.ui.theme.Theme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemBottomSheet(
    listId: String,
    onDismiss: () -> Unit,
    onAddItem: (String, Double, String, Double?, String?) -> Unit
) {
    var productName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var estimatedPrice by remember { mutableStateOf("") }
    var selectedUnit by remember { mutableStateOf("un") }
    var selectedCategory by remember { mutableStateOf("Hortifruti") }
    var showSuggestions by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }

    val units = listOf("un", "kg", "g", "L", "ml")
    val categories = listOf("Hortifruti", "Laticínios", "Padaria", "Carnes", "Peixaria", "Bebidas", "Limpeza", "Doces", "Enlatados", "Mercearia", "Congelados")

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .background(MaterialTheme.colorScheme.surface, RectangleShape)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Drag handle
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(MaterialTheme.colorScheme.outline, RectangleShape)
                )

                Text(text = "Adicionar Item", fontSize = 20.sp, fontWeight = FontWeight.Bold)

                // Product name with search
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "Nome do produto", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                    TextField(
                        value = productName,
                        onValueChange = { productName = it; showSuggestions = it.isNotBlank() },
                        label = { Text("🔍  Nome do produto") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        trailingIcon = {
                            if (productName.isNotBlank()) {
                                IconButton(onClick = { productName = "" }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Clear")
                                }
                            }
                        }
                    )

                    // Suggestions
                    if (showSuggestions && productName.isNotBlank()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = androidx.compose.material3.CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                getSuggestions(productName).forEach { suggestion ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                            .background(Color.Transparent, RectangleShape)
                                            .clip(RectangleShape),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            painter = painterResource(id = getCategoryIcon(suggestion.category)),
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        androidx.compose.foundation.layout.Spacer(Modifier.width(8.dp))
                                        Text(text = suggestion.name, fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Quantity and Price row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "Quantidade", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                        TextField(
                            value = quantity,
                            onValueChange = { quantity = it },
                            label = { Text("Quantidade") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                        )
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "Preço estimado", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                        TextField(
                            value = estimatedPrice,
                            onValueChange = { estimatedPrice = it },
                            label = { Text("Preço estimado") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                            leadingIcon = { Text("R$ ", fontSize = 16.sp) }
                        )
                    }
                }

                // Unit chips
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "Unidade", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        units.forEach { unit ->
                            UnitChip(
                                text = unit,
                                selected = selectedUnit == unit,
                                onClick = { selectedUnit = unit }
                            )
                        }
                    }
                }

                // Category chips
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "Categoria", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.TopStart
                    ) {
                        androidx.compose.foundation.layout.FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            categories.forEach { category ->
                                UnitChip(
                                    text = category,
                                    selected = selectedCategory == category,
                                    onClick = { selectedCategory = category }
                                )
                            }
                        }
                    }
                }

                // Notes
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "Notas (opcional)", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                    TextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Ex: pegar maduro") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                // Add button
                Button(
                    onClick = {
                        onAddItem(
                            productName,
                            quantity.toDoubleOrNull() ?: 1.0,
                            selectedUnit,
                            estimatedPrice.toDoubleOrNull(),
                            selectedCategory
                        )
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = productName.isNotBlank(),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(text = "Adicionar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun UnitChip(text: String, selected: Boolean, onClick: () -> Unit) {
    androidx.compose.material3.Chip(
        onClick = onClick,
        selected = selected,
        modifier = Modifier.height(36.dp).wrapContentWidth(),
        colors = androidx.compose.material3.ChipDefaults.chipColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Text(text = text, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

private data class Suggestion(
    val name: String,
    val category: String
)

private fun getSuggestions(query: String): List<Suggestion> {
    val allProducts = listOf(
        Suggestion("Tomate", "Hortifruti"),
        Suggestion("Leite Integral", "Laticínios"),
        Suggestion("Pão Francês", "Padaria"),
        Suggestion("Cebola", "Hortifruti"),
        Suggestion("Alface", "Hortifruti"),
        Suggestion("Cenoura", "Hortifruti"),
        Suggestion("Banana", "Hortifruti"),
        Suggestion("Maçã", "Hortifruti"),
        Suggestion("Arroz", "Mercearia"),
        Suggestion("Feijão", "Mercearia")
    )
    return allProducts.filter { it.name.lowercase().contains(query.lowercase()) }.take(5)
}

private fun getCategoryIcon(category: String): Int {
    return when (category.lowercase()) {
        "hortifruti" -> androidx.compose.ui.res.R.drawable.ic_local_grocery_store
        "laticínios", "laticinios" -> androidx.compose.ui.res.R.drawable.ic_local_drink
        "padaria" -> androidx.compose.ui.res.R.drawable.ic_bakery_dining
        "carnes" -> androidx.compose.ui.res.R.drawable.ic_set_meal
        "peixaria" -> androidx.compose.ui.res.R.drawable.ic_seafood
        "bebidas" -> androidx.compose.ui.res.R.drawable.ic_local_bar
        "limpeza" -> androidx.compose.ui.res.R.drawable.ic_cleaning_services
        "doces" -> androidx.compose.ui.res.R.drawable.ic_cake
        "enlatados" -> androidx.compose.ui.res.R.drawable.ic_kitchen
        "mercearia" -> androidx.compose.ui.res.R.drawable.ic_shopping_basket
        "congelados" -> androidx.compose.ui.res.R.drawable.ic_ac_unit
        else -> androidx.compose.ui.res.R.drawable.ic_help_outline
    }
}

companion object {
    @Composable
    fun show(listId: String, onDismiss: () -> Unit, onAddItem: (String, Double, String, Double?, String?) -> Unit) {
        // This would be called from a BottomSheetScaffold or ModalBottomSheetLayout
        AddItemBottomSheet(listId = listId, onDismiss = onDismiss, onAddItem = onAddItem)
    }
}