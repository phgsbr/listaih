package com.listaih.app.ui.screens.additem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SetMeal
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val units = listOf("un", "kg", "g", "L", "ml")
    val categories = listOf("Hortifruti", "Laticínios", "Padaria", "Carnes", "Peixaria", "Bebidas", "Limpeza", "Doces", "Enlatados", "Mercearia", "Congelados")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, bottom = 32.dp)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "Adicionar Item", fontSize = 20.sp, fontWeight = FontWeight.Bold)

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                OutlinedTextField(
                    value = productName,
                    onValueChange = { productName = it; showSuggestions = it.isNotBlank() },
                    label = { Text("Nome do produto") },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (productName.isNotBlank()) {
                            IconButton(onClick = { productName = "" }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Limpar")
                            }
                        }
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                )

                if (showSuggestions && productName.isNotBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = androidx.compose.material3.CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            getSuggestions(productName).forEach { suggestion ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = getCategoryIcon(suggestion.category),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text(text = suggestion.name, fontSize = 14.sp)
                                    Spacer(Modifier.weight(1f))
                                    Text(
                                        text = suggestion.category,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantidade") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = estimatedPrice,
                    onValueChange = { estimatedPrice = it },
                    label = { Text("Preço estimado") },
                    leadingIcon = { Text("R$ ", fontSize = 16.sp) },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.weight(1f)
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Unidade",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Categoria",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                FlowRow(
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

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notas (opcional)") },
                placeholder = { Text("Ex: pegar maduro") },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            )

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
                enabled = productName.isNotBlank(),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(text = "Adicionar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun UnitChip(text: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text = text, fontSize = 14.sp, fontWeight = FontWeight.Medium) },
        modifier = Modifier.height(36.dp).wrapContentWidth(),
        colors = FilterChipDefaults.filterChipColors()
    )
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

private fun getCategoryIcon(category: String): ImageVector {
    return when (category.lowercase()) {
        "hortifruti" -> Icons.Filled.LocalGroceryStore
        "laticínios", "laticinios" -> Icons.Filled.LocalDrink
        "padaria" -> Icons.Filled.BakeryDining
        "carnes" -> Icons.Filled.SetMeal
        "peixaria" -> Icons.Filled.DinnerDining
        "bebidas" -> Icons.Filled.LocalBar
        "limpeza" -> Icons.Filled.CleaningServices
        "doces" -> Icons.Filled.Cake
        "enlatados" -> Icons.Filled.Kitchen
        "mercearia" -> Icons.Filled.ShoppingBasket
        "congelados" -> Icons.Filled.AcUnit
        else -> Icons.Filled.HelpOutline
    }
}

@Composable
fun ShowAddItemBottomSheet(
    listId: String,
    onDismiss: () -> Unit = {},
    onAddItem: (String, Double, String, Double?, String?) -> Unit = { _, _, _, _, _ -> }
) {
    AddItemBottomSheet(listId = listId, onDismiss = onDismiss, onAddItem = onAddItem)
}