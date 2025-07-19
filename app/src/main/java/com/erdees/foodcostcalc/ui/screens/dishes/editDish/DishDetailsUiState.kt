package com.erdees.foodcostcalc.ui.screens.dishes.editDish

import android.icu.util.Currency
import com.erdees.foodcostcalc.domain.model.ScreenState
import com.erdees.foodcostcalc.domain.model.UsedItem
import com.erdees.foodcostcalc.domain.model.dish.DishDomain

/**
 * Represents the UI state for the DishDetails screen.
 */
data class DishDetailsUiState(
    val dish: DishDomain? = null,
    val editableFields: EditableFields = EditableFields(),
    val screenState: ScreenState = ScreenState.Idle,
    val showCopyConfirmation: Boolean = false,
    val currentlyEditedItem: UsedItem? = null,
    val currency: Currency? = null,
){
    val items: List<UsedItem> = (dish?.products ?: listOf()) + (dish?.halfProducts ?: listOf())
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
