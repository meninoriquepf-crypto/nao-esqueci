package com.naoesqueci.app.presentation.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.naoesqueci.app.R
import com.naoesqueci.app.domain.model.CheckState
import com.naoesqueci.app.domain.model.ItemCategory
import com.naoesqueci.app.domain.model.ItemWithState
import com.naoesqueci.app.domain.model.TripItem
import com.naoesqueci.app.domain.model.TripType

@Composable
fun ChecklistSection(
    tripId: Long,
    tripType: TripType,
    items: List<ItemWithState>,
    onItemClick: (TripItem, Boolean) -> Unit,
    onItemLongClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val normalItems = items.filter {
        it.item.category == ItemCategory.NORMAL && it.item.requiredFor(tripType)
    }
    val giftItems = items.filter { it.item.category == ItemCategory.GIFT }

    Column(modifier = modifier.fillMaxWidth()) {
        if (normalItems.isNotEmpty()) {
            SectionHeader(
                title = stringResource(R.string.trip_detail_items_title),
                count = "${normalItems.count { it.checkState?.isChecked == true }} de ${normalItems.size}"
            )
            normalItems.forEach { itemWithState ->
                ItemRow(
                    item = itemWithState.item,
                    checkState = itemWithState.checkState,
                    isGift = false,
                    onClick = { onItemClick(itemWithState.item, !(itemWithState.checkState?.isChecked ?: false)) },
                    onLongClick = { onItemLongClick(itemWithState.item.id!!) }
                )
            }
            Spacer(Modifier.height(16.dp))
        }

        if (giftItems.isNotEmpty()) {
            SectionHeader(
                title = stringResource(R.string.trip_detail_gifts_title),
                count = "${giftItems.count { it.checkState?.isChecked == true }} de ${giftItems.size}"
            )
            giftItems.forEach { itemWithState ->
                ItemRow(
                    item = itemWithState.item,
                    checkState = itemWithState.checkState,
                    isGift = true,
                    onClick = { onItemClick(itemWithState.item, !(itemWithState.checkState?.isChecked ?: false)) },
                    onLongClick = { onItemLongClick(itemWithState.item.id!!) }
                )
            }
        }

        if (normalItems.isEmpty() && giftItems.isEmpty()) {
            EmptyState(
                icon = R.drawable.ic_notification,
                title = stringResource(R.string.trip_detail_empty_items),
                subtitle = null
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItemRow(
    item: TripItem,
    checkState: CheckState?,
    isGift: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isChecked = checkState?.isChecked ?: false
    val textColor = MaterialTheme.colorScheme.onSurface
    val checkColor = MaterialTheme.colorScheme.primary

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = null,
                    colors = CheckboxDefaults.colors(
                        checkedColor = checkColor,
                        uncheckedColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.size(24.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    if (isGift) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CardGiftcard,
                                contentDescription = "Presente",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.bodyLarge,
                                color = textColor
                            )
                        }
                    } else {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = textColor
                        )
                    }
                }
            }

            if (!isGift) {
                ParticipationIndicator(
                    requiredForDeparture = item.requiredForDeparture,
                    requiredForReturn = item.requiredForReturn
                )
            }
        }
    }
}