package com.erdees.foodcostcalc.domain.model

import androidx.annotation.Keep

@Keep
data class DishDomain(
  val dishId: Long,
  val name: String,
  val marginPercent: Double,
  val dishTax: Double,
  val products: List<ProductDomain>,
  val halfProducts: List<HalfProductDomain>
)
