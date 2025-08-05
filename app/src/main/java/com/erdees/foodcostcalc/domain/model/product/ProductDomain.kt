package com.erdees.foodcostcalc.domain.model.product

import androidx.annotation.Keep
import com.erdees.foodcostcalc.domain.model.Item
import com.erdees.foodcostcalc.domain.model.units.MeasurementUnit
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class ProductDomain(
    override val id: Long,
    override val name: String,

    // User's original input (for display and editing)
    val inputMethod: InputMethod,
    val packagePrice: Double?,
    val packageQuantity: Double?,
    val packageUnit: MeasurementUnit?,

    val canonicalPrice: Double,
    val canonicalUnit: MeasurementUnit,

    // Existing fields
    val tax: Double,
    val waste: Double,
) : Item {

    private val priceWithTax = canonicalPrice * (1 + tax / 100)

    val priceAfterWasteAndTax =
        priceWithTax / (1 - waste / 100)

}