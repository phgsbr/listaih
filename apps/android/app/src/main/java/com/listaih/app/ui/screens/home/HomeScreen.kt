package com.listaih.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.listaih.app.R

data class ShoppingListUi(
    val id: String,
    val name: String,
    val icon: String,
    val checkedItems: Int,
    val totalItems: Int,
    val estimatedTotal: Double,
    val hasOnlineMembers: Boolean,
    val members: List<String>,
    val archived: Boolean = false,
    val isModel: Boolean = false,
    val listType: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    lists: List<ShoppingListUi>,
    onListClick: (String, String) -> Unit,
    onAddListClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onHistoryClick: () -> Unit,
    isLoading: Boolean = false
) {
    val filterAll = stringResource(R.string.home_filter_all)
    val filterActive = stringResource(R.string.home_filter_active)
    val filterArchived = stringResource(R.string.home_filter_archived)
    val filterTemplates = stringResource(R.string.home_filter_templates)

    var selectedTab by remember { mutableStateOf(0) }
    var selectedFilter by remember { mutableStateOf(filterAll) }
    var query by remember { mutableStateOf("") }
    var searching by remember { mutableStateOf(false) }
    val filters = listOf(filterAll, filterActive, filterArchived, filterTemplates)

    val baseLists = when {
        selectedTab == 1 -> lists.filter { it.archived }
        else -> when (selectedFilter) {
            filterActive -> lists.filter { !it.archived && !it.isModel }
            filterArchived -> lists.filter { it.archived }
            filterTemplates -> lists.filter { it.isModel }
            else -> lists
        }
    }

    val visibleLists = if (query.isNotBlank()) {
        baseLists.filter { it.name.contains(query.trim(), ignoreCase = true) }
    } else {
        baseLists
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (searching) {
                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            placeholder = { Text(stringResource(R.string.home_search_placeholder), fontSize = 15.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(text = stringResource(R.string.home_title), fontSize = 20.sp, fontWeight = FontWeight.Medium)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        searching = !searching
                        if (!searching) query = ""
                    }) {
                        Icon(
                            imageVector = if (searching) Icons.Filled.Clear else Icons.Filled.Search,
                            contentDescription = if (searching) stringResource(R.string.home_search_close_cd) else stringResource(R.string.home_search_cd)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddListClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.home_add_list_cd))
            }
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.fillMaxWidth()
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                    label = { Text(stringResource(R.string.home_tab_home)) },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.History, contentDescription = null) },
                    label = { Text(stringResource(R.string.home_tab_history)) },
                    selected = selectedTab == 1,
                    onClick = { onHistoryClick() }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                    label = { Text(stringResource(R.string.home_tab_settings)) },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2; onSettingsClick() }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                items(filters) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter; selectedTab = 0 },
                        label = { Text(filter, fontWeight = FontWeight.Medium) }
                    )
                }
            }

            if (isLoading && visibleLists.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (visibleLists.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Filled.Home,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = if (selectedTab == 1) stringResource(R.string.home_empty_archived) else stringResource(R.string.home_empty_active),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (selectedTab == 0) {
                            Text(
                                text = stringResource(R.string.home_empty_tip),
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    items(visibleLists) { list ->
                        ListCard(
                            list = list,
                            onClick = { onListClick(list.id, list.name) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ListCard(list: ShoppingListUi, onClick: () -> Unit) {
    val done = list.checkedItems == list.totalItems && list.totalItems > 0
    val inProgress = list.checkedItems > 0 && !done

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Icon(
                            painter = painterResource(id = when (list.icon) {
                                "shopping_cart" -> R.drawable.ic_shopping_cart
                                "celebration" -> R.drawable.ic_celebration
                                "medication" -> R.drawable.ic_medication
                                else -> R.drawable.ic_launcher_foreground
                            }),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier
                                .size(22.dp)
                                .align(Alignment.Center)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = list.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                done -> MaterialTheme.colorScheme.primary
                                inProgress -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.surfaceContainerHighest
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (done) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = stringResource(R.string.home_card_done_cd),
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text(
                            text = "${list.checkedItems}/${list.totalItems}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                inProgress -> MaterialTheme.colorScheme.onTertiary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 12.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                done -> MaterialTheme.colorScheme.primary
                                inProgress -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.surfaceContainerHighest
                            }
                        )
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (done) {
                        stringResource(R.string.home_card_all_purchased, String.format("%.2f", list.estimatedTotal))
                    } else {
                        stringResource(R.string.home_card_remaining, list.totalItems - list.checkedItems, String.format("%.2f", list.estimatedTotal))
                    },
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = when (list.listType) {
                        "RECORRENTE" -> stringResource(R.string.common_list_type_recurring)
                        "MODELO" -> stringResource(R.string.common_list_type_template)
                        else -> stringResource(R.string.common_list_type_one_time)
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (list.hasOnlineMembers) stringResource(R.string.common_online_members) else stringResource(R.string.common_only_you),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 8.dp)
                )
                AvatarStack(members = list.members, onlineCount = if (list.hasOnlineMembers) minOf(2, list.members.size) else 0)
            }
        }
    }
}

@Composable
fun AvatarStack(members: List<String>, onlineCount: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy((-12).dp)) {
        members.forEachIndexed { index, initial ->
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        when (index % 3) {
                            0 -> MaterialTheme.colorScheme.primary
                            1 -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.secondary
                        }
                    )
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}