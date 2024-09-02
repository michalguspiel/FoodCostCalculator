package com.erdees.foodcostcalc.domain.model.product

import com.erdees.foodcostcalc.domain.model.UsedItem
import com.erdees.foodcostcalc.utils.UnitsUtils.calculatePrice
import kotlinx.serialization.Serializable
import java.text.DecimalFormat

/**
 * Can be used in dish or half-product
 * */
@Serializable
data class UsedProductDomain(
    override val id: Long,
    override val ownerId: Long,
    override val item: ProductDomain,
    override val quantity: Double,
    override val quantityUnit: String,
    val weightPiece: Double?
) : UsedItem {
    val totalPrice = calculatePrice(
        item.pricePerUnit,
        quantity,
        item.unit,
        quantityUnit,
    )

    val totalWeightForPiece = weightPiece?.let {
        it * quantity
    }

    private val formattedTotalPrice: String = DecimalFormat("#.##").format(totalPrice)

    override fun toString(): String {
        return "${item.name}, quantity: $quantity, total price: $formattedTotalPrice"
    }
}
