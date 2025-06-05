package com.erdees.foodcostcalc.domain.model.product

import androidx.annotation.Keep
import com.erdees.foodcostcalc.domain.model.Item
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class ProductDomain(
  override val id: Long,
  override val name: String,
  val pricePerUnit: Double,
  val tax: Double,
  val waste: Double,
  val unit: String,
) : Item {

  private val priceWithTax = pricePerUnit * (1 + tax / 100)

  val priceAfterWasteAndTax =
    priceWithTax / (1 - waste / 100)

  override fun toString(): String {
    return "$name, price $unit netto: $pricePerUnit.\nPrice $unit with foodcost: $priceAfterWasteAndTax."
  }
}