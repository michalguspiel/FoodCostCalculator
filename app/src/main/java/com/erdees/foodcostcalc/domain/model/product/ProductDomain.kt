package com.erdees.foodcostcalc.domain.model.product

import androidx.annotation.Keep
import com.erdees.foodcostcalc.domain.model.Item
import kotlinx.serialization.Serializable
import java.text.DecimalFormat

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

  private val formattedBruttoPrice: String = DecimalFormat("#.##").format(priceAfterWasteAndTax)

  override fun toString(): String {
    return "$name, price $unit netto: $pricePerUnit.\nPrice $unit with foodcost: $formattedBruttoPrice."
  }
}