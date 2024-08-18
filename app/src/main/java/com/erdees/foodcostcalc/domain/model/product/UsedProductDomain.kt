package com.erdees.foodcostcalc.domain.model.product

import com.erdees.foodcostcalc.domain.model.product.ProductDomain
import com.erdees.foodcostcalc.utils.UnitsUtils.calculatePrice
import java.text.DecimalFormat

/**
 * Can be used in dish or half-product
 * */
data class UsedProductDomain(
  val product: ProductDomain,
  val quantity : Double,
  val quantityUnit: String,
  val weightPiece: Double?
){
  val totalPrice = calculatePrice(
    product.pricePerUnit,
    quantity,
    product.unit,
    quantityUnit,
  )

  val df = DecimalFormat("#.##")

  val formattedTotalPrice: String = df.format(totalPrice)

  override fun toString(): String {
    return "${product.name}, quantity: $quantity, total price: $formattedTotalPrice"
  }
}
