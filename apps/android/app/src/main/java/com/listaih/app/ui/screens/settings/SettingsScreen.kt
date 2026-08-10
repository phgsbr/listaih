package com.listaih.app.ui.screens.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DoorFront
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
    val darkTheme by viewModel.darkTheme.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val hapticFeedback by viewModel.hapticFeedback.collectAsState()
    val offlineMode by viewModel.offlineMode.collectAsState()
    val language by viewModel.language.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val householdName by viewModel.householdName.collectAsState()
    val inviteCode by viewModel.inviteCode.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val message by viewModel.message.collectAsState()

    var showLanguageDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showInviteDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var exportedJson by remember { mutableStateOf("") }

    val context = LocalContext.current

    fun copyToClipboard(text: String, label: String = "Código") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
        Toast.makeText(context, if (label == "Código") "Código copiado!" else "Dados copiados!", Toast.LENGTH_SHORT).show()
    }

    LaunchedEffect(message) {
        message?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.swipeMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Configurações", fontSize = 20.sp, fontWeight = FontWeight.Medium) },
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

            SettingsSection(title = "Casa") {
                SettingsItem(
                    icon = Icons.Filled.FamilyRestroom,
                    title = "Minha Casa",
                    subtitle = householdName.ifBlank { "carregando..." }
                )
                SettingsItem(
                    icon = Icons.Filled.Code,
                    title = "Código de convite",
                    subtitle = inviteCode.ifBlank { "carregando..." },
                    onClick = { if (inviteCode.isNotBlank()) showInviteDialog = true }
                )
                SettingsItem(
                    icon = Icons.Filled.RestartAlt,
                    title = "Gerar novo código",
                    subtitle = "Invalida o código anterior",
                    enabled = inviteCode.isNotBlank() && !loading,
                    onClick = { viewModel.regenerateInviteCode() }
                )
            }

            SettingsSection(title = "Localização & Moeda") {
                SettingsItem(
                    icon = Icons.Filled.Language,
                    title = "Idioma",
                    subtitle = when (language) {
                        "pt-BR" -> "Português (Brasil)"
                        "en-US" -> "English (US)"
                        "es-ES" -> "Español (España)"
                        else -> language
                    },
                    onClick = { showLanguageDialog = true }
                )
                SettingsItem(
                    icon = Icons.Filled.AttachMoney,
                    title = "Moeda",
                    subtitle = when (currency) {
                        "BRL" -> "Real Brasileiro (BRL)"
                        "USD" -> "Dólar Americano (USD)"
                        "EUR" -> "Euro (EUR)"
                        else -> currency
                    },
                    onClick = { showCurrencyDialog = true }
                )
            }

            SettingsSection(title = "Aparência") {
                SettingsItem(
                    icon = Icons.Filled.Checkroom,
                    title = "Tema escuro",
                    trailing = {
                        Switch(
                            checked = darkTheme,
                            onCheckedChange = { viewModel.setDarkTheme(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                )
            }

            SettingsSection(title = "Notificações") {
                SettingsItem(
                    icon = Icons.Filled.Notifications,
                    title = "Notificações push",
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
                    title = "Feedback tátil",
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

            SettingsSection(title = "Backup") {
                SettingsItem(
                    icon = Icons.Filled.Backup,
                    title = "Exportar dados",
                    subtitle = "Gera um backup JSON dos dados locais",
                    enabled = !loading,
                    onClick = {
                        viewModel.exportData { json ->
                            if (json != null) {
                                exportedJson = json
                                showExportDialog = true
                            }
                        }
                    }
                )
            }

            SettingsSection(title = "Sobre") {
                SettingsItem(
                    icon = Icons.Filled.Info,
                    title = "Versão",
                    subtitle = "1.0.0",
                    onClick = { showAboutDialog = true }
                )
                SettingsItem(
                    icon = Icons.Filled.Security,
                    title = "Privacidade & segurança",
                    onClick = { showPrivacyDialog = true }
                )
            }

            SettingsSection(title = "Zona de perigo") {
                SettingsItem(
                    icon = Icons.Filled.DeleteSweep,
                    title = "Limpar listas arquivadas",
                    subtitle = "Irreversível",
                    titleColor = MaterialTheme.colorScheme.error,
                    enabled = false
                )
                SettingsItem(
                    icon = Icons.Filled.RestartAlt,
                    title = "Resetar integrações",
                    subtitle = "Remove Grocy, HA, Alexa",
                    titleColor = MaterialTheme.colorScheme.error,
                    enabled = false
                )
                SettingsItem(
                    icon = Icons.Filled.DoorFront,
                    title = "Resetar sistema",
                    subtitle = "Apaga tudo e reinicia setup",
                    titleColor = MaterialTheme.colorScheme.error,
                    enabled = false
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
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
    }

    if (showLanguageDialog) {
        ChoiceDialog(
            title = "Idioma",
            options = listOf("pt-BR" to "Português (Brasil)", "en-US" to "English (US)", "es-ES" to "Español (España)"),
            selected = language,
            onDismiss = { showLanguageDialog = false },
            onSelect = { viewModel.setLanguage(it); showLanguageDialog = false }
        )
    }

    if (showCurrencyDialog) {
        ChoiceDialog(
            title = "Moeda",
            options = listOf("BRL" to "Real Brasileiro (BRL)", "USD" to "Dólar Americano (USD)", "EUR" to "Euro (EUR)"),
            selected = currency,
            onDismiss = { showCurrencyDialog = false },
            onSelect = { viewModel.setCurrency(it); showCurrencyDialog = false }
        )
    }

    if (showInviteDialog) {
        AlertDialog(
            onDismissRequest = { showInviteDialog = false },
            title = { Text("Código de convite") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Compartilhe este código para adicionar alguém à sua casa:",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = inviteCode,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 6.sp
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { copyToClipboard(inviteCode); showInviteDialog = false }) {
                    Text("Copiar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showInviteDialog = false }) {
                    Text("Fechar")
                }
            }
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
                                validationError = "As senhas não coincidem"
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

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Backup gerado") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Seus dados locais foram exportados como JSON. Copie o conteúdo abaixo ou compartilhe.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = exportedJson,
                            fontSize = 11.sp,
                            maxLines = 8,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { copyToClipboard(exportedJson, "Backup"); showExportDialog = false }) {
                    Text("Copiar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Fechar")
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
                    Text("Versão 1.0.0")
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
            title = { Text("Privacidade & segurança") },
            text = {
                Text(
                    "Seus dados são armazenados localmente no seu servidor. Nenhuma informação é compartilhada com terceiros.",
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
            text = { Text("Você precisará entrar novamente para acessar suas listas.") },
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