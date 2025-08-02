package com.erdees.foodcostcalc.ui.screens.dishes.forms.newcomponent

import com.erdees.foodcostcalc.domain.model.units.MeasurementUnit

data class NewProductFormData(
    val purchasePrice: String = "",
    val purchaseUnit: MeasurementUnit? = null,
    val wastePercent: String = "",
    val quantityAddedToDish: String = "",
    val unitForDish: MeasurementUnit? = null,
)
