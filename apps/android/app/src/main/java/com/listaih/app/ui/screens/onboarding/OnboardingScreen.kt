package com.listaih.app.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.listaih.app.R

@Composable
fun OnboardingScreen(
    onLogin: () -> Unit,
    onSetup: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showServerDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.size(48.dp))

            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(120.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "Listaih logo",
                        modifier = Modifier.size(72.dp)
                    )
                }
            }

            Spacer(Modifier.size(32.dp))

            Text(
                text = "Listaih",
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.size(8.dp))

            Text(
                text = "Sua lista de compras colaborativa e inteligente",
                fontSize = 16.sp,
                lineHeight = 24.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.size(40.dp))

            FeatureRow(
                icon = Icons.Filled.Groups,
                title = "Listas colaborativas",
                subtitle = "Crie listas e compartilhe com sua família em tempo real"
            )
            FeatureRow(
                icon = Icons.Filled.QrCodeScanner,
                title = "Modo compras",
                subtitle = "Marque itens no mercado com escaneamento de código de barras"
            )
            FeatureRow(
                icon = Icons.Filled.VerifiedUser,
                title = "Controle total",
                subtitle = "Self-hosted, sua casa, seus dados, sua privacidade"
            )

            Spacer(Modifier.size(40.dp))

            Button(
                onClick = { viewModel.checkSetup(onLogin, onSetup) },
                enabled = !uiState.setupChecking,
                modifier = Modifier
                    .fillMaxWidth()
                    .size(56.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                if (uiState.setupChecking) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(text = "Entrar ou criar conta", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.size(12.dp))

            OutlinedButton(
                onClick = { showServerDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.CloudQueue,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(text = "Conectar ao servidor", fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(Modifier.size(12.dp))

            Text(
                text = "Servidor: ${uiState.serverUrl}",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            uiState.setupError?.let { msg ->
                Spacer(Modifier.size(8.dp))
                Text(
                    text = msg,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.size(32.dp))
        }
    }

    if (showServerDialog) {
        var url by remember { mutableStateOf(uiState.serverUrl) }
        var testMessage by remember { mutableStateOf<String?>(null) }
        var testSuccess by remember { mutableStateOf<Boolean?>(null) }
        var testing by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showServerDialog = false },
            title = { Text("Conectar ao servidor") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Endereço do servidor Listaih (ex: http://192.168.0.10:3000). A mudança vale imediatamente.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = url,
                        onValueChange = { url = it; testMessage = null },
                        label = { Text("URL") },
                        singleLine = true,
                        placeholder = { Text("http://127.0.0.1:3000") }
                    )
                    testMessage?.let {
                        Text(
                            text = it,
                            fontSize = 13.sp,
                            color = if (testSuccess == true) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(
                        enabled = !testing,
                        onClick = {
                            viewModel.saveServerUrl(url.trim())
                            showServerDialog = false
                        }
                    ) {
                        Text("Salvar")
                    }
                    TextButton(
                        enabled = !testing,
                        onClick = {
                            testing = true
                            testMessage = null
                            viewModel.testConnection(url.trim()) { ok ->
                                testing = false
                                testSuccess = ok
                                testMessage = if (ok) "Conexão OK" else "Falha na conexão (verifique a URL)"
                            }
                        }
                    ) {
                        if (testing) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Testar conexão")
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showServerDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun FeatureRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.size(2.dp))
            Text(
                text = subtitle,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}