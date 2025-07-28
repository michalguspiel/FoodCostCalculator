package com.erdees.foodcostcalc.ui.screens.dishes.editDish

import android.content.Context
import com.erdees.foodcostcalc.domain.model.InteractionType
import com.erdees.foodcostcalc.domain.model.ItemUsageEntry
import com.erdees.foodcostcalc.ui.screens.dishes.forms.componentlookup.ComponentSelection
import com.erdees.foodcostcalc.ui.screens.dishes.forms.existingcomponent.ExistingItemFormData

data class DishActions(
    val saveDish: () -> Unit = {},
    val shareDish: (Context) -> Unit = {},
    val saveAndNavigate: () -> Unit = {},
    val resetScreenState: () -> Unit = {},
)

data class DishPropertyActions(
    val updateName: (String) -> Unit = {},
    val saveName: () -> Unit = {},
    val updateTax: (String) -> Unit = {},
    val saveTax: () -> Unit = {},
    val updateMargin: (String) -> Unit = {},
    val saveMargin: () -> Unit = {},
    val updateTotalPrice: (String) -> Unit = {},
    val saveTotalPrice: () -> Unit = {},
)

data class ItemActions(
    val removeItem: (ItemUsageEntry) -> Unit = {},
    val updateQuantity: (String) -> Unit = {},
    val saveQuantity: () -> Unit = {},
    val setComponentSelection: (ComponentSelection?) -> Unit = {},
    val onAddExistingComponentClick: (ExistingItemFormData) -> Unit = {},
)

data class DishDeletionActions(
    val onDeleteDishClick: () -> Unit = {},
    val onDeleteConfirmed: (Long) -> Unit = {},
)

data class DishCopyActions(
    val onCopyDishClick: () -> Unit = {},
    val copyDish: () -> Unit = {},
    val updateCopiedDishName: (String) -> Unit = {},
    val hideCopyConfirmation: () -> Unit = {},
)

data class ScreenInteractionActions(
    val setInteraction: (InteractionType) -> Unit = {},
    val discardChangesAndProceed: () -> Unit = {},
    val saveChangesAndProceed: () -> Unit = {},
)

data class DishDetailsScreenActions(
    val dishActions: DishActions = DishActions(),
    val propertyActions: DishPropertyActions = DishPropertyActions(),
    val itemActions: ItemActions = ItemActions(),
    val deletionActions: DishDeletionActions = DishDeletionActions(),
    val copyActions: DishCopyActions = DishCopyActions(),
    val interactionActions: ScreenInteractionActions = ScreenInteractionActions(),
)