package com.listaih.wear.ui.screens.voice

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
import androidx.compose.material.icons.filled.Mic
import androidx.wear.compose.material.Button

@Composable
fun WearVoiceScreen(
    onBackClick: () -> Unit
) {
    var isListening by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var recognizedText by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Microphone button
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(
                    if (isListening) Color(0xFFBA1A1A) else MaterialTheme.colorScheme.primary,
                    RectangleShape
                )
                .clip(RectangleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Mic, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(36.dp))
        }

        Text(text = if (isListening) "Ouvindo..." : "Toque para falar", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        recognizedText.ifNotBlank {
            Text(text = it, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, RectangleShape)
                .clip(RectangleShape)
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    isListening = !isListening
                    if (!isListening) {
                        // Simulate recognition
                        recognizedText = "Tomate, 2 kg"
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.wear.compose.material.ButtonDefaults.buttonColors(
                    containerColor = if (isListening) Color(0xFFBA1A1A) else MaterialTheme.colorScheme.primary
                )
            ) {
                Text(text = if (isListening) "Parar" : "Iniciar", fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }

            Button(
                onClick = onBackClick,
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.wear.compose.material.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            ) {
                Text(text = "Cancelar", fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}