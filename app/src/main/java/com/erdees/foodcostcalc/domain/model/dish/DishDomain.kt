package com.erdees.foodcostcalc.domain.model.dish

import android.util.Log
import com.erdees.foodcostcalc.domain.model.halfProduct.UsedHalfProductDomain
import com.erdees.foodcostcalc.domain.model.product.UsedProductDomain
import kotlinx.serialization.Serializable


@Serializable
data class DishDomain(
  val dishId: Long,
  val name: String,
  val marginPercent: Double,
  val taxPercent: Double,
  val products: List<UsedProductDomain>,
  val halfProducts: List<UsedHalfProductDomain>
) {
  val foodCost: Double =
    products.sumOf {
      it.totalPrice.also { totalPrice ->
        Log.i("DishDomain", "Product: ${it}, quantity : ${it.quantity}, totalPrice: $totalPrice")
      }
    } + halfProducts.sumOf {
      it.totalPrice.also { totalPrice ->
        Log.i(
          "DishDomain",
          "Half product: ${it}, quantity : ${it.quantity}, totalPrice: $totalPrice"
        )
      }
    }

  val totalPrice: Double
    get() {
      val priceWithMargin = foodCost * marginPercent / 100
      val amountOfTax = priceWithMargin * taxPercent / 100
      return priceWithMargin + amountOfTax
    }

  fun finalPricePerServing(amountOfServings: Int): Double {
    return totalPrice * amountOfServings
  }
}
