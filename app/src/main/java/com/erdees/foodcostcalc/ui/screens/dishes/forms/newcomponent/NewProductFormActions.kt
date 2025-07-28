package com.erdees.foodcostcalc.ui.screens.dishes.forms.newcomponent

data class NewProductFormActions(
    val onFormDataUpdate: (NewProductFormData) -> Unit = {},
    val onProductCreationDropdownExpandedChange: (Boolean) -> Unit = {},
    val onProductAdditionDropdownExpandedChange: (Boolean) -> Unit = {},
    val onSaveProduct: (NewProductFormData) -> Unit = {},
)
