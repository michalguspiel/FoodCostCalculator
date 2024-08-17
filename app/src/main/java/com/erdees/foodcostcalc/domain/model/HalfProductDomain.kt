package com.erdees.foodcostcalc.domain.model

import android.content.Context
import com.erdees.foodcostcalc.utils.Format.df
import com.erdees.foodcostcalc.utils.UnitsUtils
import com.erdees.foodcostcalc.utils.Utils

data class HalfProductDomain(
  override val id: Long,
  override val name: String,
  val halfProductUnit: String,
  val products: List<UsedProductDomain>
) : Item {
  val totalPrice = products.sumOf { it.totalPrice }

  val totalQuantity =
    if (halfProductUnit == "per piece") 1.0
    else products.map {
      UnitsUtils.computeWeightAndVolumeToSameUnit(
        halfProductUnit,
        it.quantityUnit,
        it.quantity
      )
    }.sum()

  val formattedPrice: String = df.format(totalPrice)

  fun pricePerUnit(): Double {
    val totalPrice = products.sumOf { it.totalPrice }
    return when (halfProductUnit) {
      "per piece" -> totalPrice
      else -> totalPrice / totalQuantity
    }
  }

  fun formattedPricePerUnit(context: Context): String = Utils.formatPrice(pricePerUnit(), context)

  fun formattedPricePerRecipe(context: Context): String = Utils.formatPrice(totalPrice, context)
}
