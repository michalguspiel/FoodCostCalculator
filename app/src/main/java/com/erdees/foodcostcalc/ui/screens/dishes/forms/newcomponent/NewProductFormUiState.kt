package com.erdees.foodcostcalc.ui.screens.dishes.forms.newcomponent

import com.erdees.foodcostcalc.domain.model.units.MeasurementUnit

data class NewProductFormUiState(
    val productName: String = "",
    val dishName: String = "",
    val productCreationUnits: Set<MeasurementUnit> = emptySet(),
    val productAdditionUnits: Set<MeasurementUnit> = emptySet(),
    val formData: NewProductFormData = NewProductFormData(),
    val isAddButtonEnabled: Boolean = false,
    val productCreationDropdownExpanded: Boolean = false,
    val productAdditionDropdownExpanded: Boolean = false,
)