package com.erdees.foodcostcalc.domain.model.product

import androidx.annotation.Keep
import com.erdees.foodcostcalc.domain.model.ItemUsageEntry
import com.erdees.foodcostcalc.domain.model.units.MeasurementUnit
import com.erdees.foodcostcalc.utils.UnitsUtils.calculatePrice
import kotlinx.serialization.Serializable

/**
 * Can be used in dish or half-product
 * */
@Keep
@Serializable
data class ProductAddedToDish(
    override val item: ProductDomain,
    override val quantity: Double,
    override val quantityUnit: MeasurementUnit,
) : ItemUsageEntry {
    override val foodCost = calculatePrice(
        item.priceAfterWasteAndTax,
        quantity,
        item.canonicalUnit,
        quantityUnit,
    )
}