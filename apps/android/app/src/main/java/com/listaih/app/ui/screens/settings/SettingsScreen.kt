package com.listaih.app.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.listaih.app.ui.theme.Theme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Switch

@Composable
fun SettingsScreen(onLogout: () -> Unit) {
    var darkTheme by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var hapticFeedback by remember { mutableStateOf(true) }
    var offlineMode by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Configurações", fontSize = 20.sp, fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = { /* TODO: Back */ }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Profile Section
            SettingsSection(title = "Perfil") {
                SettingsItem(
                    icon = Icons.Filled.Person,
                    title = "João Silva",
                    subtitle = "joao@exemplo.com",
                    onClick = { /* TODO: Edit profile */ }
                )
                SettingsItem(
                    icon = Icons.Filled.Settings,
                    title = "Alterar senha",
                    onClick = { /* TODO: Change password */ }
                )
            }

            // Household Section
            SettingsSection(title = "Casa") {
                SettingsItem(
                    icon = Icons.Filled.Settings,
                    title = "Minha Casa",
                    subtitle = "Casa da Família Silva",
                    onClick = { /* TODO: Household settings */ }
                )
                SettingsItem(
                    icon = Icons.Filled.Settings,
                    title = "Código de convite",
                    subtitle = "ABC123",
                    onClick = { /* TODO: Show invite code */ }
                )
            }

            // Language & Currency
            SettingsSection(title = "Localização & Moeda") {
                SettingsItem(
                    icon = Icons.Filled.Language,
                    title = "Idioma",
                    subtitle = "Português (Brasil)",
                    trailing = {
                        androidx.compose.material3.MenuAnchor() // Placeholder for dropdown
                    }
                )
                SettingsItem(
                    icon = Icons.Filled.AttachMoney,
                    title = "Moeda",
                    subtitle = "Real Brasileiro (BRL)",
                    trailing = {
                        androidx.compose.material3.MenuAnchor() // Placeholder for dropdown
                    }
                )
            }

            // Appearance
            SettingsSection(title = "Aparência") {
                SettingsItem(
                    icon = Icons.Filled.Settings,
                    title = "Tema escuro",
                    trailing = {
                        Switch(
                            checked = darkTheme,
                            onCheckedChange = { darkTheme = it },
                            colors = androidx.compose.material3.SwitchDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                )
            }

            // Notifications
            SettingsSection(title = "Notificações") {
                SettingsItem(
                    icon = Icons.Filled.Settings,
                    title = "Notificações push",
                    trailing = {
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { notificationsEnabled = it },
                            colors = androidx.compose.material3.SwitchDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                )
                SettingsItem(
                    icon = Icons.Filled.Settings,
                    title = "Feedback tátil",
                    trailing = {
                        Switch(
                            checked = hapticFeedback,
                            onCheckedChange = { hapticFeedback = it },
                            colors = androidx.compose.material3.SwitchDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                )
            }

            // Offline mode
            SettingsSection(title = "Offline") {
                SettingsItem(
                    icon = Icons.Filled.Settings,
                    title = "Modo offline",
                    subtitle = "Sincronizar quando conectar",
                    trailing = {
                        Switch(
                            checked = offlineMode,
                            onCheckedChange = { offlineMode = it },
                            colors = androidx.compose.material3.SwitchDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                )
            }

            // Backup
            SettingsSection(title = "Backup") {
                SettingsItem(
                    icon = Icons.Filled.Backup,
                    title = "Exportar dados",
                    subtitle = "Baixar backup JSON",
                    onClick = { /* TODO: Export */ }
                )
            }

            // About
            SettingsSection(title = "Sobre") {
                SettingsItem(
                    icon = Icons.Filled.Info,
                    title = "Versão",
                    subtitle = "1.0.0",
                    onClick = { /* TODO: About dialog */ }
                )
                SettingsItem(
                    icon = Icons.Filled.Info,
                    title = "Licenças open source",
                    onClick = { /* TODO: Licenses */ }
                )
            }

            // Danger Zone
            SettingsSection(title = "Zona de perigo") {
                SettingsItem(
                    icon = Icons.Filled.Warning,
                    title = "Sair da conta",
                    subtitle = "Remove dados locais",
                    titleColor = MaterialTheme.colorScheme.error,
                    onClick = onLogout
                )
                SettingsItem(
                    icon = Icons.Filled.Warning,
                    title = "Limpar listas arquivadas",
                    subtitle = "Irreversível",
                    titleColor = MaterialTheme.colorScheme.error,
                    enabled = false
                )
                SettingsItem(
                    icon = Icons.Filled.Warning,
                    title = "Resetar integrações",
                    subtitle = "Remove Grocy, HA, Alexa",
                    titleColor = MaterialTheme.colorScheme.error,
                    enabled = false
                )
                SettingsItem(
                    icon = Icons.Filled.Warning,
                    title = "Resetar sistema",
                    subtitle = "Apaga tudo e reinicia setup",
                    titleColor = MaterialTheme.colorScheme.error,
                    enabled = false
                )
            }
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title.uppercase(),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, top = 16.dp, bottom = 4.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                content()
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    titleColor: Color = Color.Unspecified,
    trailing: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val itemColor = titleColor.takeOrElse { MaterialTheme.colorScheme.onSurface }
    val itemAlpha = if (enabled) 1f else 0.5f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .background(if (enabled) Color.Transparent else MaterialTheme.colorScheme.surfaceContainer, RectangleShape)
            .clip(RectangleShape)
            .alpha(itemAlpha),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = itemColor.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp)
            )
            androidx.compose.foundation.layout.Spacer(Modifier.width(16.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = itemColor)
                subtitle?.let {
                    Text(text = it, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )

        if (trailing != null) {
            trailing()
        } else if (onClick != null) {
            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(20.dp))
        }
    }
}