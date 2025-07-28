package com.erdees.foodcostcalc.ui.screens.dishes.forms.newcomponent

data class NewProductFormUiState(
    val productName: String = "",
    val dishName: String = "",
    val productCreationUnits: Set<String> = emptySet(),
    val productAdditionUnits: Set<String> = emptySet(),
    val formData: NewProductFormData = NewProductFormData(),
    val isAddButtonEnabled: Boolean = false,
    val productCreationDropdownExpanded: Boolean = false,
    val productAdditionDropdownExpanded: Boolean = false,
)