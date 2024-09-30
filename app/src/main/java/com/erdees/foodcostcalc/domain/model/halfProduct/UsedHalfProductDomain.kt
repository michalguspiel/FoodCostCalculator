package com.erdees.foodcostcalc.domain.model.halfProduct

import androidx.annotation.Keep
import com.erdees.foodcostcalc.domain.model.UsedItem
import com.erdees.foodcostcalc.utils.UnitsUtils
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class UsedHalfProductDomain(
    override val id: Long,
    override val ownerId: Long,
    override val item: HalfProductDomain,
    override val quantity: Double,
    override val quantityUnit: String
) : UsedItem {

    override val totalPrice = UnitsUtils.calculatePrice(
        item.pricePerUnit,
        quantity,
        item.halfProductUnit,
        quantityUnit
    )
}