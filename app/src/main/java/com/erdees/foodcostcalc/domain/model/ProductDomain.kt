package com.erdees.foodcostcalc.domain.model

import androidx.annotation.Keep
import java.text.DecimalFormat

@Keep
data class ProductDomain(
  override val id: Long,
  override val name: String,
  val pricePerUnit: Double,
  val tax: Double,
  val waste: Double,
  val unit: String,
): Item {
  val priceAfterWasteAndTax =
    pricePerUnit + pricePerUnit * (waste / 100) + pricePerUnit * (tax / 100)

  val df = DecimalFormat("#.##")

  val formattedBruttoPrice: String = df.format(priceAfterWasteAndTax)

  override fun toString(): String {
    return "$name, price $unit netto: $pricePerUnit.\nPrice $unit with foodcost: $formattedBruttoPrice."
  }
}
