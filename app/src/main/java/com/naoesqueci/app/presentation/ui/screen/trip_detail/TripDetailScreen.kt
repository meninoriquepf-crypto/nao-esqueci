package com.naoesqueci.app.presentation.ui.screen.trip_detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.naoesqueci.app.R
import com.naoesqueci.app.domain.model.ItemCategory
import com.naoesqueci.app.domain.model.TripType
import com.naoesqueci.app.presentation.ui.component.ChecklistSection
import com.naoesqueci.app.presentation.ui.component.SvgIcon
import com.naoesqueci.app.presentation.ui.component.TripHeader
import com.naoesqueci.app.presentation.ui.component.TripTypeToggle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(
    viewModel: TripDetailViewModel,
    onEditTrip: () -> Unit,
    onAddItem: () -> Unit,
    onEditItem: (Long) -> Unit,
    onTripDeleted: () -> Unit
) {
    val trip by viewModel.trip.collectAsState(initial = null)
    val departureItems by viewModel.departureItems.collectAsState(initial = emptyList())
    val returnItems by viewModel.returnItems.collectAsState(initial = emptyList())
    val uiState by viewModel.uiState.collectAsState()
    var showMenu by remember { mutableStateOf(false) }

    trip?.let { currentTrip ->
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(currentTrip.name) },
                    actions = {
                        IconButton(onClick = onEditTrip) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar viagem")
                        }
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Mais opcoes")
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Excluir viagem") },
                                onClick = {
                                    showMenu = false
                                    viewModel.confirmDeleteTrip()
                                }
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = onAddItem,
                    icon = { SvgIcon(R.drawable.ic_add, "Adicionar item", Modifier.size(24.dp)) },
                    text = { Text("Item") }
                )
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                TripHeader(trip = currentTrip)

                TripTypeToggle(
                    selectedType = uiState.selectedType,
                    onTypeChange = { viewModel.setSelectedType(it) }
                )

                Spacer(Modifier.height(8.dp))

                val currentItems = when (uiState.selectedType) {
                    TripType.DEPARTURE -> departureItems
                    TripType.RETURN -> returnItems
                }

                val requiredItems = currentItems.filter {
                    it.item.category == ItemCategory.NORMAL && it.item.requiredFor(uiState.selectedType)
                }
                val checkedCount = requiredItems.count { it.checkState?.isChecked == true }

                if (requiredItems.isNotEmpty()) {
                    TripProgressSection(checked = checkedCount, total = requiredItems.size)
                    Spacer(Modifier.height(8.dp))
                }

                ChecklistSection(
                    tripId = currentTrip.id ?: return@Scaffold,
                    tripType = uiState.selectedType,
                    items = currentItems,
                    onItemClick = { item, isChecked ->
                        viewModel.toggleCheck(item.id!!, uiState.selectedType, isChecked)
                    },
                    onItemLongClick = { itemId -> onEditItem(itemId) }
                )
            }
        }
    }

    if (uiState.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelDeleteTrip() },
            title = { Text(stringResource(R.string.trip_delete_confirm)) },
            text = { Text(stringResource(R.string.trip_delete_message)) },
            confirmButton = {
                Button(onClick = { viewModel.executeDeleteTrip(onSuccess = onTripDeleted) }) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { viewModel.cancelDeleteTrip() }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun TripProgressSection(
    checked: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (checked >= total) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = stringResource(R.string.trip_detail_all_checked),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        } else {
            Text(
                text = stringResource(R.string.trip_detail_progress, checked, total),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LinearProgressIndicator(
                progress = checked.toFloat() / total.toFloat(),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}