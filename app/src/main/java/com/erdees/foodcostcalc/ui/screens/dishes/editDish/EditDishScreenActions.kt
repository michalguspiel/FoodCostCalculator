package com.erdees.foodcostcalc.ui.screens.dishes.editDish

import android.content.Context
import com.erdees.foodcostcalc.domain.model.InteractionType
import com.erdees.foodcostcalc.domain.model.ItemUsageEntry
import com.erdees.foodcostcalc.ui.screens.dishes.forms.componentlookup.ComponentSelection
import com.erdees.foodcostcalc.ui.screens.dishes.forms.existingcomponent.ExistingItemFormData

data class EditDishScreenActions(
    val saveDish: () -> Unit = {},
    val shareDish: (Context) -> Unit = {},
    val setInteraction: (InteractionType) -> Unit = {},
    val removeItem: (ItemUsageEntry) -> Unit = {},
    val updateQuantity: (String) -> Unit = {},
    val saveQuantity: () -> Unit = {},
    val updateTax: (String) -> Unit = {},
    val saveTax: () -> Unit = {},
    val updateMargin: (String) -> Unit = {},
    val saveMargin: () -> Unit = {},
    val updateName: (String) -> Unit = {},
    val saveName: () -> Unit = {},
    val updateTotalPrice: (String) -> Unit = {},
    val saveTotalPrice: () -> Unit = {},
    val resetScreenState: () -> Unit = {},
    val onDeleteDishClick: () -> Unit = {},
    val onDeleteConfirmed: (Long) -> Unit = {},
    val saveAndNavigate: () -> Unit = {},
    val onCopyDishClick: () -> Unit = {},
    val copyDish: () -> Unit = {},
    val updateCopiedDishName: (String) -> Unit = {},
    val hideCopyConfirmation: () -> Unit = {},
    val discardChangesAndProceed: () -> Unit = {},
    val saveChangesAndProceed: () -> Unit = {},
    val setComponentSelection: (ComponentSelection?) -> Unit = {},
    val onAddExistingComponentClick: (ExistingItemFormData) -> Unit = {},

    )

data class ExistingComponentFormActions(
    val onFormDataChange: (ExistingItemFormData) -> Unit = {},
    val onUnitForDishDropdownExpandedChange: (Boolean) -> Unit = {},
    val onAddComponent: (ExistingItemFormData) -> Unit = {},
    val onCancel: () -> Unit = {},
)