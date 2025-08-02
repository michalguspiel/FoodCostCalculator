package com.erdees.foodcostcalc.domain.model.product

import android.icu.util.Currency
import androidx.annotation.Keep
import com.erdees.foodcostcalc.domain.model.UsedItem
import com.erdees.foodcostcalc.domain.model.units.MeasurementUnit
import com.erdees.foodcostcalc.utils.UnitsUtils.calculatePrice
import com.erdees.foodcostcalc.utils.Utils
import kotlinx.serialization.Serializable

/**
 * Can be used in dish or half-product
 * */
@Keep
@Serializable
data class UsedProductDomain(
    override val id: Long,
    override val ownerId: Long,
    override val item: ProductDomain,
    override val quantity: Double,
    override val quantityUnit: MeasurementUnit,
    val weightPiece: Double?
) : UsedItem {
    override val foodCost = calculatePrice(
        item.priceAfterWasteAndTax,
        quantity,
        item.unit,
        quantityUnit,
    )

    fun formattedTotalPriceForTargetQuantity(
        baseQuantity: Double,
        targetQuantity: Double,
        currency: Currency?
    ): String {
        val percentageOfBaseQuantity = targetQuantity * 100 / baseQuantity
        val adjustedPrice = foodCost * (percentageOfBaseQuantity / 100)
        return Utils.formatPrice(adjustedPrice, currency)
    }
}