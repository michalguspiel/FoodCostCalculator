package com.erdees.foodcostcalc.ui.screens.dishes.forms.existingcomponent

import com.erdees.foodcostcalc.domain.model.units.MeasurementUnit

data class ExistingComponentFormUiState(
    val formData: ExistingItemFormData = ExistingItemFormData(),
    val compatibleUnitsForDish: Set<MeasurementUnit> = emptySet(),
    val unitForDishDropdownExpanded: Boolean = false,
    val isAddButtonEnabled: Boolean = false
)