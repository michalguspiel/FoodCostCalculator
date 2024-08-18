package com.erdees.foodcostcalc.domain.model.dish

import com.erdees.foodcostcalc.domain.model.halfProduct.UsedHalfProductDomain
import com.erdees.foodcostcalc.domain.model.product.UsedProductDomain


data class DishDomain(
  val dishId: Long,
  val name: String,
  val marginPercent: Double,
  val dishTax: Double,
  val products: List<UsedProductDomain>,
  val halfProducts: List<UsedHalfProductDomain>
) {
  val totalPrice: Double =
    products.sumOf { it.totalPrice } + halfProducts.sumOf { it.totalPrice }

  val margin: Double
    get() = totalPrice * marginPercent / 100

  val tax: Double
    get() = totalPrice * dishTax / 100

  val priceWithMarginAndTax: Double
    get() {
      val priceWithMargin = totalPrice * margin / 100
      val amountOfTax = priceWithMargin * tax / 100
      return priceWithMargin + amountOfTax
    }

  fun finalPricePerServing(amountOfServings: Int): Double {
    return priceWithMarginAndTax * amountOfServings
  }

}
