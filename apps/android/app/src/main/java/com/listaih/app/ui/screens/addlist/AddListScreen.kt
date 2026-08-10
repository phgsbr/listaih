package com.listaih.app.ui.screens.addlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ListIcon(
    val name: String,
    val label: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddListScreen(
    onBackClick: () -> Unit,
    onCreate: (String, String, String) -> Unit
) {
    var listName by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("shopping_cart") }
    var selectedType by remember { mutableStateOf("PONTUAL") }

    val icons = listOf(
        ListIcon("shopping_cart", "Compras", Icons.Filled.ShoppingCart),
        ListIcon("celebration", "Festas", Icons.Filled.Celebration),
        ListIcon("medication", "Farmácia", Icons.Filled.Medication)
    )

    val listTypes = listOf(
        ListTypeOption("PONTUAL", "Pontual", "Uma única compra. Arquiva automaticamente no checkout."),
        ListTypeOption("RECORRENTE", "Recorrente", "Compra que se repete. Zera o status no checkout.")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Nova lista", fontSize = 20.sp, fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(text = "Dê um nome à sua nova lista", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            OutlinedTextField(
                value = listName,
                onValueChange = { listName = it },
                label = { Text("Nome da lista") },
                placeholder = { Text("Ex: Supermercado do mês") },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Tipo de lista",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            listTypes.forEach { option ->
                FilterChip(
                    selected = selectedType == option.value,
                    onClick = { selectedType = option.value },
                    label = {
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(option.label, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text(option.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Text(
                text = "Ícone",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                icons.forEach { icon ->
                    FilterChip(
                        selected = selectedIcon == icon.name,
                        onClick = { selectedIcon = icon.name },
                        label = { Text(icon.label, fontSize = 14.sp, fontWeight = FontWeight.Medium) },
                        leadingIcon = {
                            Icon(icon.icon, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    )
                }
            }

            Button(
                onClick = { onCreate(listName, selectedType, selectedIcon) },
                enabled = listName.isNotBlank(),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text(text = "Criar lista", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

data class ListTypeOption(
    val value: String,
    val label: String,
    val description: String
)