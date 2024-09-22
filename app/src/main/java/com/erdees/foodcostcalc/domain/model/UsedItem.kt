package com.erdees.foodcostcalc.domain.model

import android.content.Context
import com.erdees.foodcostcalc.utils.UnitsUtils
import com.erdees.foodcostcalc.utils.Utils
import java.text.DecimalFormat

interface UsedItem {
    val id: Long
    val ownerId: Long
    val item: Item
    val quantity: Double
    val quantityUnit: String

    val totalPrice: Double

    fun formattedTotalPricePerServing(context: Context, amountOfServings: Double): String =
        Utils.formatPrice(totalPrice * amountOfServings, context)

    fun formatQuantityForTargetServing(servings: Double): String =
        "${DecimalFormat("#.##").format(quantity * servings)} ${
            UnitsUtils.getUnitAbbreviation(
                quantityUnit
            )
        }"

    fun formatQuantityForTargetServing(
        baseQuantity: Double,
        targetQuantity: Double
    ): String {
        val percentageOfBaseQuantity = targetQuantity * 100 / baseQuantity
        val adjustedQuantity = quantity * (percentageOfBaseQuantity / 100)
        return "${DecimalFormat("#.##").format(adjustedQuantity)} ${
            UnitsUtils.getUnitAbbreviation(
                quantityUnit
            )
        }"
    }
}