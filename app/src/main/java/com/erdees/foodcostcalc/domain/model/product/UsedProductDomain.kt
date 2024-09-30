package com.erdees.foodcostcalc.domain.model.product

import android.content.Context
import androidx.annotation.Keep
import com.erdees.foodcostcalc.domain.model.UsedItem
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
    override val quantityUnit: String,
    val weightPiece: Double?
) : UsedItem {
    override val totalPrice = calculatePrice(
        item.priceAfterWasteAndTax,
        quantity,
        item.unit,
        quantityUnit,
    )

    fun formattedTotalPriceForTargetQuantity(
        baseQuantity: Double,
        targetQuantity: Double,
        context: Context
    ): String {
        val percentageOfBaseQuantity = targetQuantity * 100 / baseQuantity
        val adjustedPrice = totalPrice * (percentageOfBaseQuantity / 100)
        return Utils.formatPrice(adjustedPrice, context)
    }
}