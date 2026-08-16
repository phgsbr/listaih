package com.listaih.app.ui.screens.detail

import com.listaih.app.data.scanner.HapticFeedback
import com.listaih.app.data.scanner.LocalBtScanner
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.SetMeal
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.listaih.app.R
import com.listaih.app.data.scanner.ScreenWake
import com.listaih.app.ui.screens.additem.ShowAddItemBottomSheet
import com.listaih.app.ui.scanpopup.AssociableItem
import com.listaih.app.ui.scanpopup.ScanPopupController
import com.listaih.app.ui.scanpopup.ScanPopupHost
import com.listaih.app.ui.screens.shopping.BarcodeScannerScreen

@Composable
fun ListDetailScreen(
    listId: String,
    listName: String,
    onBackClick: () -> Unit,
    onShoppingModeClick: () -> Unit = {},
    viewModel: ListDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val items = uiState.items
    val title = uiState.title

    var showAddItem by remember { mutableStateOf(false) }
    var showScanner by remember { mutableStateOf(false) }
    var showRename by remember { mutableStateOf(false) }
    var renameText by remember { mutableStateOf(listName) }
    var editingItem by remember { mutableStateOf<ListItemUi?>(null) }
    var moreMenuExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val btScanner = LocalBtScanner.current
    val scanController = remember { ScanPopupController() }

    fun findScanItem(barcode: String): ScanPopupController.ScanItem? {
        val current = viewModel.uiState.value.items
        return current.firstOrNull { it.barcode == barcode }?.let {
            ScanPopupController.ScanItem(
                id = it.id,
                name = it.name,
                quantity = it.quantity.trim().split(" ").firstOrNull()?.toDoubleOrNull() ?: 1.0,
                unit = it.quantity.trim().split(" ").drop(1).joinToString(" ").ifBlank { "un" },
                estimatedPrice = it.estimatedPrice,
                category = it.category,
                barcode = it.barcode,
            )
        }
    }

    fun handleBarcode(barcode: String) {
        when (val decision = scanController.handleBarcode(barcode, ::findScanItem)) {
            is ScanPopupController.ScanDecision.Check ->
                viewModel.confirmScannedItem(decision.itemId, decision.quantity)
            is ScanPopupController.ScanDecision.Increment ->
                viewModel.incrementScannedItem(decision.itemId, decision.quantity)
            is ScanPopupController.ScanDecision.Dismiss -> Unit
            null -> Unit
        }
        when (scanController.state) {
            is ScanPopupController.ScanPopupState.Unrecognized -> {
                HapticFeedback.error(context)
                ScreenWake.wake(context)
            }
            is ScanPopupController.ScanPopupState.Recognized -> {
                HapticFeedback.success(context)
                ScreenWake.clear(context)
            }
            null -> ScreenWake.clear(context)
        }
        showScanner = false
    }

    val currentHandleBarcode by androidx.compose.runtime.rememberUpdatedState(::handleBarcode)

    DisposableEffect(btScanner) {
        btScanner?.onBarcodeScanned = { barcode -> currentHandleBarcode(barcode) }
        onDispose {
            btScanner?.onBarcodeScanned = null
            ScreenWake.clear(context)
        }
    }

    val checkedCount = items.count { it.checked }
    val progress = if (items.isNotEmpty()) checkedCount.toFloat() / items.size else 0f

    fun toggleCheck(id: String) {
        val current = items.firstOrNull { it.id == id } ?: return
        viewModel.toggleCheck(id, current.checked)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.detail_back_cd))
                    }
                },
                actions = {
                    IconButton(onClick = { showScanner = true }) {
                        Icon(Icons.Filled.QrCodeScanner, contentDescription = stringResource(R.string.detail_scan_cd))
                    }
                    IconButton(onClick = onShoppingModeClick) {
                        Icon(Icons.Filled.ShoppingCart, contentDescription = stringResource(R.string.detail_checkout_cd))
                    }
                    Box {
                        IconButton(onClick = { moreMenuExpanded = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.detail_more_cd))
                        }
                        DropdownMenu(
                            expanded = moreMenuExpanded,
                            onDismissRequest = { moreMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.detail_menu_rename)) },
                                leadingIcon = { Icon(Icons.Filled.EditNote, contentDescription = null) },
                                onClick = {
                                    renameText = title
                                    moreMenuExpanded = false
                                    showRename = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.detail_menu_check_all)) },
                                leadingIcon = { Icon(Icons.Filled.Check, contentDescription = null) },
                                onClick = {
                                    viewModel.checkAll()
                                    moreMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.detail_menu_uncheck_all)) },
                                leadingIcon = { Icon(Icons.Filled.ContentCopy, contentDescription = null) },
                                onClick = {
                                    viewModel.uncheckAll()
                                    moreMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.detail_menu_delete_checked)) },
                                leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                                onClick = {
                                    viewModel.deleteChecked()
                                    moreMenuExpanded = false
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddItem = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.detail_add_item_cd))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ScanPopupHost(
                state = scanController.state,
                items = items.map {
                    AssociableItem(id = it.id, name = it.name, category = it.category)
                },
                busy = uiState.isScanBusy,
                onConfirmRecognized = {
                    scanController.confirmCurrent()?.let { decision ->
                        if (decision is ScanPopupController.ScanDecision.Check) {
                            viewModel.confirmScannedItem(decision.itemId, decision.quantity)
                        }
                    }
                    ScreenWake.clear(context)
                },
                onDismiss = {
                    scanController.clear()
                    ScreenWake.clear(context)
                },
                onAssociate = { itemId ->
                    val barcode = (scanController.state as? ScanPopupController.ScanPopupState.Unrecognized)?.barcode
                    if (barcode != null) {
                        viewModel.associateScannedItem(itemId, barcode)
                    }
                    scanController.clear()
                    ScreenWake.clear(context)
                },
                onCreateNew = { name ->
                    val barcode = (scanController.state as? ScanPopupController.ScanPopupState.Unrecognized)?.barcode
                    if (barcode != null) {
                        viewModel.createScannedProduct(barcode, name)
                    }
                    scanController.clear()
                    ScreenWake.clear(context)
                },
                onCreateGeneric = {
                    val barcode = (scanController.state as? ScanPopupController.ScanPopupState.Unrecognized)?.barcode
                    if (barcode != null) {
                        viewModel.createGenericScannedProduct(barcode)
                    }
                    scanController.clear()
                    ScreenWake.clear(context)
                },
                onSuggestName = { barcode -> viewModel.suggestScannedName(barcode) }
            )

            if (items.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.detail_header_items_count, items.size),
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = stringResource(R.string.detail_header_purchased_count, checkedCount),
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        androidx.compose.material3.LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }

            if (uiState.isLoading && items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Filled.ShoppingBasket,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = stringResource(R.string.detail_empty_title),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(R.string.detail_empty_subtitle),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    val openItems = items.filter { !it.checked }
                    val boughtItems = items.filter { it.checked }

                    openItems.groupBy { it.category }.forEach { (category, categoryItems) ->
                        item(key = "header_$category") {
                            CategoryHeader(category = category, icon = getCategoryIcon(category))
                        }
                        items(categoryItems) { item ->
                            ListItemRow(
                                item = item,
                                onCheckClick = { toggleCheck(item.id) },
                                onClick = { editingItem = item }
                            )
                        }
                    }

                    if (boughtItems.isNotEmpty()) {
                        item(key = "header_comprados") {
                            CategoryHeader(category = stringResource(R.string.detail_category_purchased), icon = Icons.Filled.Check)
                        }
                        items(boughtItems) { item ->
                            ListItemRow(
                                item = item,
                                onCheckClick = { toggleCheck(item.id) },
                                onClick = { editingItem = item }
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 96.dp, bottom = 16.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TotalRow(label = stringResource(R.string.detail_total_estimated), value = "R$ ${String.format("%.2f", items.sumOf { it.estimatedPrice ?: 0.0 })}", bold = false)
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            TotalRow(label = stringResource(R.string.detail_total_spent), value = "R$ ${String.format("%.2f", items.filter { it.checked }.sumOf { it.actualPrice ?: 0.0 })}", bold = true, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
            }
        }
    }

    if (showAddItem) {
        ShowAddItemBottomSheet(
            listId = listId,
            onDismiss = { showAddItem = false },
            onAddItem = { name, quantity, unit, price, category ->
                if (name.isNotBlank()) {
                    viewModel.addItem(name, quantity, unit, price, category)
                }
                showAddItem = false
            }
        )
    }

    editingItem?.let { editing ->
        EditItemDialog(
            item = editing,
            onDismiss = { editingItem = null },
            onSave = { id, name, quantity, price, category ->
                val parts = quantity.trim().split(Regex("\\s+"))
                val qty = parts.firstOrNull()?.toDoubleOrNull() ?: 1.0
                val unit = parts.drop(1).joinToString(" ").ifBlank { "un" }
                viewModel.updateItem(id, name, qty, unit, price, category)
                editingItem = null
            },
            onDelete = { id ->
                viewModel.deleteItem(id)
                editingItem = null
            }
        )
    }

    if (showRename) {
        AlertDialog(
            onDismissRequest = { showRename = false },
            title = { Text(stringResource(R.string.detail_rename_dialog_title)) },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    label = { Text("Nome da lista") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (renameText.isNotBlank()) viewModel.renameList(renameText)
                        showRename = false
                    }
                ) { Text("Salvar") }
            },
            dismissButton = {
                TextButton(onClick = { showRename = false }) { Text("Cancelar") }
            }
        )
    }

    if (showScanner) {
        BarcodeScannerScreen(
            onBackClick = { showScanner = false },
            onBarcodeScanned = ::handleBarcode
        )
    }
}

@Composable
fun MemberAvatars(members: List<String>, onlineCount: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy((-10).dp)) {
        members.forEachIndexed { index, initial ->
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = initial, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

@Composable
fun CategoryHeader(category: String, icon: ImageVector) {
    val color = headerColor(category)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(MaterialTheme.shapes.small)
                .background(color.copy(alpha = 0.15f))
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp).align(Alignment.Center)
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(text = category.uppercase(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (category == "COMPRADOS") MaterialTheme.colorScheme.onSurfaceVariant else color)
        Spacer(Modifier.weight(1f))
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

@Composable
private fun headerColor(category: String): Color {
    return if (category == "Comprados") {
        MaterialTheme.colorScheme.primary
    } else {
        getCategoryColor(category)
    }
}

@Composable
fun ListItemRow(
    item: ListItemUi,
    onCheckClick: () -> Unit,
    onClick: () -> Unit
) {
    val categoryColor = getCategoryColor(item.category)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (item.checked) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        if (item.checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest,
                        CircleShape
                    )
                    .border(
                        if (item.checked) 0.dp else 2.dp,
                        MaterialTheme.colorScheme.outline,
                        CircleShape
                    )
                    .clickable(onClick = onCheckClick),
                contentAlignment = Alignment.Center
            ) {
                if (item.checked) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Icon(
                imageVector = getCategoryIcon(item.category),
                contentDescription = null,
                tint = categoryColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = item.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (item.checked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        textDecoration = if (item.checked) TextDecoration.LineThrough else TextDecoration.None
                    )
                )
                Text(text = item.quantity, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "R$ ${String.format("%.2f", item.estimatedPrice ?: 0.0)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (item.checked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.secondary
                )
                item.checkedBy?.let { by ->
                    Text(text = "✓ por $by", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun EditItemDialog(
    item: ListItemUi,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Double?, String) -> Unit,
    onDelete: (String) -> Unit
) {
    var name by remember(item.id) { mutableStateOf(item.name) }
    var quantity by remember(item.id) { mutableStateOf(item.quantity) }
    var price by remember(item.id) { mutableStateOf(item.estimatedPrice?.toString() ?: "") }
    var category by remember(item.id) { mutableStateOf(item.category) }
    val categories = listOf("Hortifruti", "Laticínios", "Padaria", "Carnes", "Peixaria", "Bebidas", "Limpeza", "Doces", "Enlatados", "Mercearia", "Congelados", "Sem Categoria")
    var categoryExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantidade") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Preço estimado (R$)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Box {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoria") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { categoryExpanded = true }
                    )
                    DropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c) },
                                onClick = {
                                    category = c
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        item.id,
                        name.ifBlank { item.name },
                        quantity.ifBlank { item.quantity },
                        price.toDoubleOrNull(),
                        category
                    )
                },
                enabled = name.isNotBlank()
            ) { Text("Salvar") }
        },
        dismissButton = {
            Row {
                TextButton(onClick = { onDelete(item.id) }) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onDismiss) { Text("Cancelar") }
            }
        }
    )
}

@Composable
fun TotalRow(label: String, value: String, bold: Boolean = false, color: Color = Color.Unspecified) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = if (bold) 16.sp else 14.sp, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal, color = if (color == Color.Unspecified) MaterialTheme.colorScheme.onSurface else color)
        Text(text = value, fontSize = if (bold) 16.sp else 14.sp, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal, color = if (color == Color.Unspecified) MaterialTheme.colorScheme.onSurface else color)
    }
}

fun getCategoryIcon(category: String): ImageVector {
    return when (category.lowercase()) {
        "hortifruti", "frutas", "verduras", "legumes" -> Icons.Filled.LocalGroceryStore
        "laticínios", "laticinios", "leite", "queijo" -> Icons.Filled.LocalDrink
        "padaria", "pão", "pao" -> Icons.Filled.BakeryDining
        "carnes", "açougue", "acougue" -> Icons.Filled.SetMeal
        "peixaria", "peixe" -> Icons.Filled.DinnerDining
        "bebidas" -> Icons.Filled.LocalBar
        "limpeza" -> Icons.Filled.CleaningServices
        "doces" -> Icons.Filled.Cake
        "enlatados", "conservas" -> Icons.Filled.Kitchen
        "mercearia" -> Icons.Filled.ShoppingBasket
        "congelados" -> Icons.Filled.AcUnit
        else -> Icons.Filled.HelpOutline
    }
}

fun getCategoryColor(category: String): Color {
    return when (category.lowercase()) {
        "hortifruti", "frutas", "verduras", "legumes" -> Color(0xFF4CAF50)
        "laticínios", "laticinios", "leite", "queijo" -> Color(0xFF2196F3)
        "padaria", "pão", "pao" -> Color(0xFFFF9800)
        "carnes", "açougue", "acougue" -> Color(0xFFF44336)
        "peixaria", "peixe" -> Color(0xFF00BCD4)
        "bebidas" -> Color(0xFF9C27B0)
        "limpeza" -> Color(0xFF03A9F4)
        "doces" -> Color(0xFFE91E63)
        "enlatados", "conservas" -> Color(0xFF795548)
        "mercearia" -> Color(0xFF8BC34A)
        "congelados" -> Color(0xFF3F51B5)
        else -> Color(0xFF9E9E9E)
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
    val checkedBy: String?,
    val barcode: String? = null,
)