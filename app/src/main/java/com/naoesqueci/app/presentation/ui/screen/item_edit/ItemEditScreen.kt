package com.naoesqueci.app.presentation.ui.screen.item_edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.naoesqueci.app.R
import com.naoesqueci.app.domain.model.ItemCategory
import com.naoesqueci.app.presentation.ui.component.ParticipationChip
import com.naoesqueci.app.presentation.ui.component.TypeOption
import com.naoesqueci.app.widget.WidgetRefreshHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemEditScreen(
    viewModel: ItemEditViewModel,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val suggestions by viewModel.suggestions.collectAsState()
    var nameFieldFocused by remember { mutableStateOf(false) }
    var suggestionsExpanded by remember { mutableStateOf(false) }
    val showSuggestions = nameFieldFocused && suggestions.isNotEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (uiState.name.isNotEmpty()) R.string.item_edit_title_editar
                            else R.string.item_edit_title_novo
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Close, contentDescription = "Cancelar")
                    }
                },
                actions = {
                    if (viewModel.itemId != null) {
                        IconButton(
                            onClick = {
                                viewModel.deleteItem {
                                    WidgetRefreshHelper.requestUpdate(context)
                                    onSave()
                                }
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Excluir")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = showSuggestions && suggestionsExpanded,
                onExpandedChange = { suggestionsExpanded = it }
            ) {
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = {
                        viewModel.setName(it)
                        suggestionsExpanded = true
                    },
                    label = { Text(stringResource(R.string.item_name_hint)) },
                    placeholder = { Text(stringResource(R.string.item_name_hint)) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .onFocusChanged { nameFieldFocused = it.isFocused },
                    singleLine = true,
                    isError = uiState.error != null,
                    trailingIcon = {
                        if (showSuggestions) {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = suggestionsExpanded)
                        }
                    }
                )
                ExposedDropdownMenu(
                    expanded = showSuggestions && suggestionsExpanded,
                    onDismissRequest = { suggestionsExpanded = false }
                ) {
                    suggestions.forEach { suggestion ->
                        DropdownMenuItem(
                            text = { Text(suggestion) },
                            onClick = {
                                viewModel.setName(suggestion)
                                suggestionsExpanded = false
                            }
                        )
                    }
                }
            }

            uiState.error?.let { error ->
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(text = "Tipo", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TypeOption(
                        label = stringResource(R.string.item_type_normal),
                        description = stringResource(R.string.item_type_normal_desc),
                        icon = Icons.Default.CheckCircle,
                        selected = uiState.category == ItemCategory.NORMAL,
                        onClick = { viewModel.setCategory(ItemCategory.NORMAL) },
                        modifier = Modifier.weight(1f)
                    )
                    TypeOption(
                        label = stringResource(R.string.item_type_gift),
                        description = stringResource(R.string.item_type_gift_desc),
                        icon = Icons.Default.CardGiftcard,
                        selected = uiState.category == ItemCategory.GIFT,
                        onClick = { viewModel.setCategory(ItemCategory.GIFT) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (uiState.category == ItemCategory.NORMAL) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.item_participation_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ParticipationChip(
                            label = stringResource(R.string.item_participation_departure),
                            selected = uiState.requiredForDeparture,
                            onClick = { viewModel.setRequiredForDeparture(!uiState.requiredForDeparture) },
                            modifier = Modifier.weight(1f).height(48.dp)
                        )
                        ParticipationChip(
                            label = stringResource(R.string.item_participation_return),
                            selected = uiState.requiredForReturn,
                            onClick = { viewModel.setRequiredForReturn(!uiState.requiredForReturn) },
                            modifier = Modifier.weight(1f).height(48.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    viewModel.saveItem {
                        WidgetRefreshHelper.requestUpdate(context)
                        onSave()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.name.isNotBlank() && !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        stringResource(
                            if (uiState.name.isNotEmpty()) R.string.item_update else R.string.item_save
                        )
                    )
                }
            }
        }
    }
}