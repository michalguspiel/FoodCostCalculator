package com.erdees.foodcostcalc.ui.screens.dishes.forms.newcomponent

import com.erdees.foodcostcalc.domain.model.product.InputMethod
import com.erdees.foodcostcalc.domain.model.units.MeasurementUnit

data class NewProductFormData(
    // Step 1: Define Purchase
    val inputMethod: InputMethod = InputMethod.PACKAGE,

    // Package pricing fields
    val packagePrice: String = "",
    val packageQuantity: String = "",
    val packageUnit: MeasurementUnit? = null,

    // Unit pricing fields
    val unitPrice: String = "",
    val unitPriceUnit: MeasurementUnit? = null,

    // Waste field (Step 1)
    val wastePercent: String = "",

    // Step 2: Define Usage
    val quantityAddedToDish: String = "",
    val quantityAddedToDishUnit: MeasurementUnit? = null,
)

val NewProductFormData.purchaseUnit: MeasurementUnit?
    get() = when (inputMethod) {
        InputMethod.PACKAGE -> packageUnit
        InputMethod.UNIT -> unitPriceUnit
    }
