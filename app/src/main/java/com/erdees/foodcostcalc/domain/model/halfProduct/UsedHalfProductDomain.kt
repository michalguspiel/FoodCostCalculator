package com.erdees.foodcostcalc.domain.model.halfProduct

import com.erdees.foodcostcalc.domain.model.UsedItem
import com.erdees.foodcostcalc.utils.UnitsUtils
import kotlinx.serialization.Serializable

@Serializable
data class UsedHalfProductDomain(
  override val id: Long,
  override val ownerId: Long,
  override val item: HalfProductDomain,
  override val quantity: Double,
  override val quantityUnit: String
) : UsedItem {
  val totalPrice = UnitsUtils.calculatePrice(
    item.pricePerUnit,
    quantity,
    item.halfProductUnit,
    quantityUnit
  )
}