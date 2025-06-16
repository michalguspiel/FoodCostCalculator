package com.erdees.foodcostcalc.domain.model

import android.icu.util.Currency
import com.erdees.foodcostcalc.utils.Utils
import java.text.DecimalFormat

/**
 * Represents the details of how a specific item is being used,
 * including its quantity and calculated price for that usage,
 * before it's formally persisted with its own identity.
 */
interface ItemUsageEntry {
    val item: Item
    val quantity: Double
    val quantityUnit: String
    val foodCost: Double

    fun formattedTotalPricePerServing(amountOfServings: Double, currency: Currency?): String =
        Utils.formatPrice(foodCost * amountOfServings, currency)

    fun formatQuantityForTargetServing(servings: Double): String =
        DecimalFormat("#.##").format(quantity * servings)

    fun formatQuantityForTargetServing(
        baseQuantity: Double,
        targetQuantity: Double
    ): String {
        val percentageOfBaseQuantity = targetQuantity * 100 / baseQuantity
        val adjustedQuantity = quantity * (percentageOfBaseQuantity / 100)
        return DecimalFormat("#.##").format(adjustedQuantity)
    }
}