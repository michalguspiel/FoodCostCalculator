package com.erdees.foodcostcalc.ui.screens.dishes.forms.existingcomponent

data class ExistingComponentFormActions(
    val onFormDataChange: (ExistingItemFormData) -> Unit = {},
    val onUnitForDishDropdownExpandedChange: (Boolean) -> Unit = {},
    val onAddComponent: (ExistingItemFormData) -> Unit = {},
    val onCancel: () -> Unit = {},
)
