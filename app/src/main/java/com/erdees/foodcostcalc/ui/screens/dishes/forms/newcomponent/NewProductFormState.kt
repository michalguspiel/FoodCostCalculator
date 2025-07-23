package com.erdees.foodcostcalc.ui.screens.dishes.forms.newcomponent

data class NewProductFormState(
    val productName: String,
    val dishName: String,
    val productCreationUnits: Set<String>,
    val productAdditionUnits: Set<String>,
    val formData: NewProductFormData,
    val isAddButtonEnabled: Boolean,
    val productCreationDropdownExpanded: Boolean,
    val productAdditionDropdownExpanded: Boolean,
)