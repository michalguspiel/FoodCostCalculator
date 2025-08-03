package com.erdees.foodcostcalc.ui.composables

import android.icu.util.Currency
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Delete
import androidx.compose.material.icons.sharp.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxDefaults
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.erdees.foodcostcalc.domain.model.ItemUsageEntry
import com.erdees.foodcostcalc.domain.model.product.ProductDomain
import com.erdees.foodcostcalc.domain.model.product.UsedProductDomain
import com.erdees.foodcostcalc.domain.model.units.MeasurementUnit
import com.erdees.foodcostcalc.ui.composables.dividers.FCCDecorativeCircle
import com.erdees.foodcostcalc.ui.theme.FCCTheme
import java.util.Locale

/**
 * A composable that displays a single item in a list which can be swiped to dismiss.
 *
 * This composable uses [SwipeToDismissBox] to enable a swipe-to-remove gesture.
 * When an item is swiped away, the [onRemove] callback is triggered.
 *
 * **Important Note on State Restoration:**
 * The internal swipe state of this composable is tied directly to the specific instance
 * of the [usedItem] object passed to it, using `System.identityHashCode`.
 * If an item is removed and then subsequently restored (e.g., through an "undo" action),
 * it is crucial that a **new instance** of the `UsedItem` data class is provided.
 * If the same object instance is reused for the restored item, the composable will
 * remember its previous dismissed state and immediately trigger the [onRemove] callback again.
 * Creating a new instance (e.g., using the `.copy()` method on the data class) ensures that the
 * swipe state is correctly reset for the restored item.
 *
 * @param usedItem The data model for the item to be displayed. A new instance is required for restored items.
 * @param onRemove Callback invoked when the item is dismissed by swiping.
 * @param onEdit Callback invoked when the user taps the edit icon.
 * @param modifier The [Modifier] to be applied to this composable.
 */
@Composable
fun UsedItem(
    usedItem: ItemUsageEntry,
    currency: Currency?,
    onRemove: (ItemUsageEntry) -> Unit,
    onEdit: (ItemUsageEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentOnRemove by rememberUpdatedState(onRemove)
    val density = LocalDensity.current
    val positionalThreshold = SwipeToDismissBoxDefaults.positionalThreshold
    val swipeState = remember(System.identityHashCode(usedItem)) {
        SwipeToDismissBoxState(
            density = density,
            initialValue = SwipeToDismissBoxValue.Settled,
            confirmValueChange = {
                it == SwipeToDismissBoxValue.EndToStart
            },
            positionalThreshold = positionalThreshold
        )
    }

    LaunchedEffect(swipeState.currentValue, usedItem) {
        if (swipeState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            currentOnRemove(usedItem)
        }
    }

    SwipeToDismissBox(
        modifier = modifier.animateContentSize(),
        state = swipeState,
        backgroundContent = {
            Box(
                contentAlignment = Alignment.CenterEnd,
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = MaterialTheme.colorScheme.errorContainer)
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = usedItem.quantity.toString() + " " + stringResource(usedItem.quantityUnit.displayNameRes).lowercase(Locale.getDefault())
                        )
                        FCCDecorativeCircle(Modifier.padding(horizontal = 6.dp))
                        Text(
                            text = usedItem.formattedTotalPricePerServing(
                                1.0,
                                currency = currency
                            )
                        )
                    }

                },
                trailingContent = {
                    IconButton(onClick = { onEdit(usedItem) }) {
                        Icon(imageVector = Icons.Sharp.Edit, contentDescription = "Edit")
                    }
                })
        })
}

@Preview
@Composable
private fun UsedItemPreview() {
    FCCTheme {
        UsedItem(
            UsedProductDomain(
                id = 0, ownerId = 0, item = ProductDomain(
                    id = 1,
                    name = "Product",
                    pricePerUnit = 10.0,
                    unit = MeasurementUnit.KILOGRAM,
                    tax = 23.0,
                    waste = 20.0
                ), quantity = 1.0, quantityUnit = MeasurementUnit.KILOGRAM, weightPiece = 1.0
            ),
            currency = Currency.getInstance(Locale.getDefault()),
            modifier = Modifier,
            onEdit = {},
            onRemove = {},
        )
    }
}