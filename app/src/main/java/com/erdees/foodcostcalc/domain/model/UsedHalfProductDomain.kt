package com.erdees.foodcostcalc.domain.model

import com.erdees.foodcostcalc.utils.Format
import com.erdees.foodcostcalc.utils.UnitsUtils

data class UsedHalfProductDomain(
    val halfProductDomain: HalfProductDomain,
    val quantity: Double,
    val quantityUnit: String
){
  val totalPrice = UnitsUtils.calculatePrice(
    halfProductDomain.totalPrice,
    halfProductDomain.totalQuantity,
    halfProductDomain.halfProductUnit,
    quantityUnit)

  val formattedPrice: String = Format.df.format(totalPrice)

}
