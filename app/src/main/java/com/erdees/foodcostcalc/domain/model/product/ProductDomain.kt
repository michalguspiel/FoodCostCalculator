package com.erdees.foodcostcalc.domain.model.product

import androidx.annotation.Keep
import com.erdees.foodcostcalc.domain.model.Item
import com.erdees.foodcostcalc.utils.Format
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
  val priceAfterWasteAndTax =
    pricePerUnit + pricePerUnit * (waste / 100) + pricePerUnit * (tax / 100)

  val formattedBruttoPrice: String = Format.df.format(priceAfterWasteAndTax)

  override fun toString(): String {
    return "$name, price $unit netto: $pricePerUnit.\nPrice $unit with foodcost: $formattedBruttoPrice."
  }
}
