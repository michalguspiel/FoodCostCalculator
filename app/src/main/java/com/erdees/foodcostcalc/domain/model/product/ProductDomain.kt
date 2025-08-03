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
    val pricePerUnit: Double,
    val tax: Double,
    val waste: Double,
    val unit: MeasurementUnit, // Changed from String to MeasurementUnit
) : Item {

    private val priceWithTax = pricePerUnit * (1 + tax / 100)

    val priceAfterWasteAndTax =
        priceWithTax / (1 - waste / 100)

}