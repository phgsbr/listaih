package com.listaih.app.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onLogout: () -> Unit
) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val theme by viewModel.theme.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val hapticFeedback by viewModel.hapticFeedback.collectAsState()
    val offlineMode by viewModel.offlineMode.collectAsState()
    val language by viewModel.language.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val wearScanDetail by viewModel.wearScanDetail.collectAsState()
    val baseUrl by viewModel.baseUrl.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val message by viewModel.message.collectAsState()

    var showLanguageDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showServerDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LaunchedEffect(message) {
        message?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.swipeMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "ConfiguraÃ§Ãµes", fontSize = 20.sp, fontWeight = FontWeight.Medium) },
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
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SettingsSection(title = "Perfil") {
                SettingsItem(
                    icon = Icons.Filled.Person,
                    title = userName.ifBlank { "Seu nome" },
                    subtitle = userEmail.ifBlank { "carregando..." }
                )
                SettingsItem(
                    icon = Icons.Filled.Password,
                    title = "Alterar senha",
                    onClick = { showPasswordDialog = true }
                )
            }

            SettingsSection(title = "AparÃªncia") {
                SettingsItem(
                    icon = Icons.Filled.Palette,
                    title = "Tema",
                    subtitle = when (theme) {
                        "dark" -> "Escuro"
                        "light" -> "Claro"
                        else -> "Sistema"
                    },
                    onClick = { showThemeDialog = true }
                )
            }

            SettingsSection(title = "Servidor") {
                SettingsItem(
                    icon = Icons.Filled.Dns,
                    title = "EndereÃ§o do servidor",
                    subtitle = baseUrl,
                    onClick = { showServerDialog = true }
                )
            }

            SettingsSection(title = "LocalizaÃ§Ã£o & Moeda") {
                SettingsItem(
                    icon = Icons.Filled.Language,
                    title = "Idioma",
                    subtitle = when (language) {
                        "pt-BR" -> "PortuguÃªs (Brasil)"
                        "en-US" -> "English (US)"
                        "es-ES" -> "EspaÃ±ol (EspaÃ±a)"
                        else -> language
                    },
                    onClick = { showLanguageDialog = true }
                )
                SettingsItem(
                    icon = Icons.Filled.AttachMoney,
                    title = "Moeda",
                    subtitle = when (currency) {
                        "BRL" -> "Real Brasileiro (BRL)"
                        "USD" -> "DÃ³lar Americano (USD)"
                        "EUR" -> "Euro (EUR)"
                        else -> currency
                    },
                    onClick = { showCurrencyDialog = true }
                )
            }

            SettingsSection(title = "NotificaÃ§Ãµes") {
                SettingsItem(
                    icon = Icons.Filled.Notifications,
                    title = "NotificaÃ§Ãµes push",
                    trailing = {
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { viewModel.setNotificationsEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                )
                SettingsItem(
                    icon = Icons.Filled.Tune,
                    title = "Feedback tÃ¡til",
                    trailing = {
                        Switch(
                            checked = hapticFeedback,
                            onCheckedChange = { viewModel.setHapticFeedback(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                )
            }

            SettingsSection(title = "Offline") {
                SettingsItem(
                    icon = Icons.Filled.CloudOff,
                    title = "Modo offline",
                    subtitle = "Sincronizar quando conectar",
                    trailing = {
                        Switch(
                            checked = offlineMode,
                            onCheckedChange = { viewModel.setOfflineMode(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                )
            }

            SettingsSection(title = "Wear OS") {
                SettingsItem(
                    icon = Icons.Filled.Watch,
                    title = "Detalhar scan no relÃ³gio",
                    subtitle = "Usar o Wear OS para detalhar o scan",
                    trailing = {
                        Switch(
                            checked = wearScanDetail,
                            onCheckedChange = { viewModel.setWearScanDetail(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                )
            }

            SettingsSection(title = "Sobre") {
                SettingsItem(
                    icon = Icons.Filled.Info,
                    title = "VersÃ£o",
                    subtitle = "1.0.0",
                    onClick = { showAboutDialog = true }
                )
                SettingsItem(
                    icon = Icons.Filled.Security,
                    title = "Privacidade & seguranÃ§a",
                    onClick = { showPrivacyDialog = true }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .clickable(enabled = true, onClick = { showLogoutDialog = true })
                    .background(MaterialTheme.colorScheme.errorContainer, MaterialTheme.shapes.medium)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Icon(
                        Icons.Filled.Logout,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Sair da conta",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }

    if (showThemeDialog) {
        ChoiceDialog(
            title = "Tema",
            options = listOf(
                "system" to "Sistema",
                "dark" to "Escuro",
                "light" to "Claro"
            ),
            selected = theme,
            onDismiss = { showThemeDialog = false },
            onSelect = { viewModel.setTheme(it); showThemeDialog = false }
        )
    }

    if (showServerDialog) {
        var url by remember { mutableStateOf(baseUrl) }
        var testMessage by remember { mutableStateOf<String?>(null) }
        var testSuccess by remember { mutableStateOf<Boolean?>(null) }
        var testing by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showServerDialog = false },
            title = { Text("Servidor") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "EndereÃ§o do servidor Listaih (ex: http://192.168.0.10:3000). A mudanÃ§a vale imediatamente.",
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
                TextButton(
                    enabled = !testing,
                    onClick = {
                        viewModel.saveBaseUrl(url.trim())
                        showServerDialog = false
                    }
                ) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { showServerDialog = false }) {
                        Text("Cancelar")
                    }
                    TextButton(
                        enabled = !testing && !loading,
                        onClick = {
                            testing = true
                            testMessage = null
                            viewModel.testConnection(url.trim()) { ok ->
                                testing = false
                                testSuccess = ok
                                testMessage = if (ok) "ConexÃ£o OK" else "Falha na conexÃ£o (verifique a URL)"
                            }
                        }
                    ) {
                        if (testing) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Testar conexÃ£o")
                        }
                    }
                }
            }
        )
    }

    if (showLanguageDialog) {
        ChoiceDialog(
            title = "Idioma",
            options = listOf("pt-BR" to "PortuguÃªs (Brasil)", "en-US" to "English (US)", "es-ES" to "EspaÃ±ol (EspaÃ±a)"),
            selected = language,
            onDismiss = { showLanguageDialog = false },
            onSelect = { viewModel.setLanguage(it); showLanguageDialog = false }
        )
    }

    if (showCurrencyDialog) {
        ChoiceDialog(
            title = "Moeda",
            options = listOf("BRL" to "Real Brasileiro (BRL)", "USD" to "DÃ³lar Americano (USD)", "EUR" to "Euro (EUR)"),
            selected = currency,
            onDismiss = { showCurrencyDialog = false },
            onSelect = { viewModel.setCurrency(it); showCurrencyDialog = false }
        )
    }

    if (showPasswordDialog) {
        var currentPassword by remember { mutableStateOf("") }
        var newPassword by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        var validationError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            title = { Text("Alterar senha") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it; validationError = null },
                        label = { Text("Senha atual") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it; validationError = null },
                        label = { Text("Nova senha") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; validationError = null },
                        label = { Text("Confirmar nova senha") },
                        singleLine = true
                    )
                    validationError?.let {
                        Text(
                            text = it,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !loading,
                    onClick = {
                        when {
                            currentPassword.isBlank() || newPassword.isBlank() -> {
                                validationError = "Preencha todos os campos"
                            }
                            newPassword.length < 6 -> {
                                validationError = "A nova senha precisa ter ao menos 6 caracteres"
                            }
                            newPassword != confirmPassword -> {
                                validationError = "As senhas nÃ£o coincidem"
                            }
                            else -> {
                                showPasswordDialog = false
                                viewModel.changePassword(currentPassword, newPassword)
                            }
                        }
                    }
                ) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("Listaih") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("VersÃ£o 1.0.0")
                    Text(
                        "Listas de compras colaborativas para sua casa.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Privacidade & seguranÃ§a") },
            text = {
                Text(
                    "Seus dados sÃ£o armazenados localmente no seu servidor. Nenhuma informaÃ§Ã£o Ã© compartilhada com terceiros.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Sair da conta?") },
            text = { Text("VocÃª precisarÃ¡ entrar novamente para acessar suas listas.") },
            confirmButton = {
                TextButton(onClick = { showLogoutDialog = false; onLogout() }) {
                    Text("Sair", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
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
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 4.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    titleColor: Color = Color.Unspecified,
    trailing: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val itemColor = if (titleColor == Color.Unspecified) MaterialTheme.colorScheme.onSurface else titleColor
    val itemAlpha = if (enabled) 1f else 0.5f
    val clickModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(clickModifier)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .alpha(itemAlpha),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = itemColor,
                    modifier = Modifier.size(20.dp).align(Alignment.Center)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = itemColor)
                subtitle?.let {
                    Text(text = it, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        if (trailing != null) {
            trailing()
        } else {
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun ChoiceDialog(
    title: String,
    options: List<Pair<String, String>>,
    selected: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    var current by remember { mutableStateOf(selected) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                options.forEach { (value, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { current = value; onSelect(value) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = current == value,
                            onClick = { current = value; onSelect(value) }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(text = label, fontSize = 15.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}