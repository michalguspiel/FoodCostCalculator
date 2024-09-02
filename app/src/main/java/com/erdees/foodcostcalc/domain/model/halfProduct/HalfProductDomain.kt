package com.erdees.foodcostcalc.domain.model.halfProduct

import android.content.Context
import com.erdees.foodcostcalc.domain.model.Item
import com.erdees.foodcostcalc.domain.model.product.UsedProductDomain
import com.erdees.foodcostcalc.utils.UnitsUtils
import com.erdees.foodcostcalc.utils.Utils
import kotlinx.serialization.Serializable

@Serializable
data class HalfProductDomain(
  override val id: Long,
  override val name: String,
  val halfProductUnit: String,
  val products: List<UsedProductDomain>
) : Item {
  val totalPrice = products.sumOf { it.totalPrice }

  val totalQuantity =
    if (halfProductUnit == "per piece") 1.0
    else products.sumOf {
      if (it.quantityUnit == "piece") it.weightPiece ?: it.quantity
      else UnitsUtils.computeWeightAndVolumeToSameUnit(
        halfProductUnit,
        it.quantityUnit,
        it.quantity
      )
    }

  val pricePerUnit: Double
    get() {
      val totalPrice = products.sumOf { it.totalPrice }
      return when (halfProductUnit) {
        "per piece" -> totalPrice
        else -> totalPrice / totalQuantity
      }
    }

  fun formattedPricePerUnit(context: Context): String = Utils.formatPrice(pricePerUnit, context)

  fun formattedPricePerRecipe(context: Context): String = Utils.formatPrice(totalPrice, context)
}
