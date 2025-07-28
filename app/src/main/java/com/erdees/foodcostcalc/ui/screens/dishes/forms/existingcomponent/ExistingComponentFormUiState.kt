package com.erdees.foodcostcalc.ui.screens.dishes.forms.existingcomponent

class ExistingComponentFormUiState(
    val formData: ExistingItemFormData = ExistingItemFormData(),
    val compatibleUnitsForDish: Set<String> = emptySet(),
    val unitForDishDropdownExpanded: Boolean = false,
    val isAddButtonEnabled: Boolean = false
)