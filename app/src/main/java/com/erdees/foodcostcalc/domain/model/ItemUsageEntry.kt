package com.erdees.foodcostcalc.domain.model

import android.icu.util.Currency
import com.erdees.foodcostcalc.domain.model.units.MeasurementUnit
import com.erdees.foodcostcalc.utils.Utils
import java.text.DecimalFormat

/**
 * Base interface for items being used in recipes/dishes
 * Implemented by temporary usage entities (ProductAddedToDish, HalfProductAddedToDish)
 * and persistent usage entities (UsedProductDomain, UsedHalfProductDomain)
 */
interface ItemUsageEntry {
    val item: Item
    val quantity: Double
    val quantityUnit: MeasurementUnit
    val foodCost: Double

    fun formattedTotalPricePerServing(amountOfServings: Double, currency: Currency?): String =
        Utils.formatPrice(foodCost * amountOfServings, currency)

    fun formatQuantityForTargetServing(servings: Double): String =
        DecimalFormat("#.##").format(quantity * servings)

    @Suppress("MagicNumber")
    fun formatQuantityForTargetServing(
        baseQuantity: Double,
        targetQuantity: Double
    ): String {
        val percentageOfBaseQuantity = targetQuantity * 100 / baseQuantity
        val adjustedQuantity = quantity * (percentageOfBaseQuantity / 100)
        return DecimalFormat("#.##").format(adjustedQuantity)
    }
}
