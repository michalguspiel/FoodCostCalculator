package com.erdees.foodcostcalc.domain.model.product

import androidx.annotation.Keep
import com.erdees.foodcostcalc.domain.model.UsedItem
import com.erdees.foodcostcalc.utils.UnitsUtils.calculatePrice
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
        item.pricePerUnit,
        quantity,
        item.unit,
        quantityUnit,
    )

    val totalWeightForPiece = weightPiece?.let {
        it * quantity
    }
}