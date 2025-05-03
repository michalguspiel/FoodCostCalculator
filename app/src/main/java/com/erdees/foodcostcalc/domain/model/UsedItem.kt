package com.erdees.foodcostcalc.domain.model

import android.icu.util.Currency
import com.erdees.foodcostcalc.utils.Utils
import java.text.DecimalFormat

interface UsedItem {
    val id: Long
    val ownerId: Long
    val item: Item
    val quantity: Double
    val quantityUnit: String

    val totalPrice: Double

    fun formattedTotalPricePerServing(amountOfServings: Double, currency: Currency?): String =
        Utils.formatPrice(totalPrice * amountOfServings, currency)

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