package com.listaih.wear.ui.screens.complete

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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.listaih.wear.ui.theme.WearTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.wear.compose.material.Button

@Composable
fun WearCompleteScreen(
    listId: String,
    listName: String,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Complete icon
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(MaterialTheme.colorScheme.primary, RectangleShape)
                .clip(RectangleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(32.dp))
        }

        Text(text = "Compra finalizada!", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(text = listName, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = "8 itens comprados", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Text(text = "Total gasto", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = "R$ 42,00", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)

        Button(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, top = 12.dp),
            colors = androidx.wear.compose.material.ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(text = "Voltar ao início", fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}