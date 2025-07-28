package com.erdees.foodcostcalc.ui.screens.dishes.dishdetails

import android.icu.util.Currency
import com.erdees.foodcostcalc.domain.model.ItemUsageEntry
import com.erdees.foodcostcalc.domain.model.JustRemovedItem
import com.erdees.foodcostcalc.domain.model.ScreenState
import com.erdees.foodcostcalc.domain.model.dish.DishDomain
import com.erdees.foodcostcalc.ui.screens.dishes.forms.componentlookup.ComponentSelection

/**
 * Represents the UI state for the DishDetails screen.
 */
data class DishDetailsUiState(
    val dish: DishDomain? = null,
    val editableFields: EditableFields = EditableFields(),
    val screenState: ScreenState = ScreenState.Idle,
    val showCopyConfirmation: Boolean = false,
    val currentlyEditedItem: ItemUsageEntry? = null,
    val currency: Currency? = null,
    val lastRemovedItem: JustRemovedItem? = null,
    val componentSelection: ComponentSelection? = null,
) {
    val items: List<ItemUsageEntry> =
        (dish?.products ?: listOf()) +
                (dish?.halfProducts ?: listOf()) +
                (dish?.productsNotSaved ?: listOf()) +
                (dish?.halfProductsNotSaved ?: listOf())

}

/**
 * Contains all editable fields in the DishDetails screen.
 */
data class EditableFields(
    val name: String = "",
    val copiedDishName: String = "",
    val totalPrice: String = "",
    val quantity: String = "",
    val tax: String = "",
    val margin: String = ""
)
