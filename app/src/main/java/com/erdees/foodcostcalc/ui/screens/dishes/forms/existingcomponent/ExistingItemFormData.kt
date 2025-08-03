package com.erdees.foodcostcalc.ui.screens.dishes.forms.existingcomponent

import com.erdees.foodcostcalc.domain.model.units.MeasurementUnit

data class ExistingItemFormData(
    val quantityForDish: String = "",
    val unitForDish: MeasurementUnit? = null,
)