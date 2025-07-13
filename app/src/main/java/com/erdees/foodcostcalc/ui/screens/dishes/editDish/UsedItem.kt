package com.erdees.foodcostcalc.ui.screens.dishes.editDish

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Delete
import androidx.compose.material.icons.sharp.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.erdees.foodcostcalc.domain.model.UsedItem

@Composable
fun UsedItem(
    usedItem: UsedItem,
    onRemove: (UsedItem) -> Unit,
    onEdit: (UsedItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val swipeState = rememberSwipeToDismissBoxState()

    SwipeToDismissBox(
        modifier = modifier.animateContentSize(),
        state = swipeState,
        backgroundContent = {
            Box(
                contentAlignment = Alignment.CenterEnd,
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = MaterialTheme.colorScheme.error)
            ) {
                Icon(
                    modifier = Modifier.minimumInteractiveComponentSize(),
                    imageVector = Icons.Sharp.Delete,
                    contentDescription = null
                )
            }
        },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        content = {
            ListItem(
                colors = (ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)),
                headlineContent = {
                    Text(text = usedItem.item.name)
                },
                supportingContent = {
                    Text(text = usedItem.quantity.toString() + " " + usedItem.quantityUnit)
                },
                trailingContent = {
                    IconButton(onClick = { onEdit(usedItem) }) {
                        Icon(imageVector = Icons.Sharp.Edit, contentDescription = "Edit")
                    }
                })
        })

    when (swipeState.currentValue) {
        SwipeToDismissBoxValue.EndToStart -> {
            LaunchedEffect(swipeState) {
                swipeState.reset()
            }
            onRemove(usedItem)
        }

        SwipeToDismissBoxValue.StartToEnd -> {}

        SwipeToDismissBoxValue.Settled -> {}
    }
}