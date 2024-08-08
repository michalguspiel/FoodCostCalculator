package com.erdees.foodcostcalc.domain.mapper

import com.erdees.foodcostcalc.data.model.Product
import com.erdees.foodcostcalc.data.model.joined.CompleteDish
import com.erdees.foodcostcalc.data.model.joined.HalfProductWithProducts
import com.erdees.foodcostcalc.domain.model.DishDomain
import com.erdees.foodcostcalc.domain.model.HalfProductDomain
import com.erdees.foodcostcalc.domain.model.ProductDomain

object Mapper {
  fun CompleteDish.toDishDomain(): DishDomain {
    return DishDomain(
      dishId = dishModel.dishId,
      name = dishModel.name,
      marginPercent = dishModel.marginPercent,
      dishTax = dishModel.dishTax,
      products = products.map { it.toProductDomain() },
      halfProducts = halfProducts.map { it.toHalfProductDomain() }
    )
  }

  fun Product.toProductDomain(): ProductDomain {
    return ProductDomain(
      productId = productId,
      name = name,
      pricePerUnit = pricePerUnit,
      tax = tax,
      waste = waste,
      unit = unit
    )
  }

  fun HalfProductWithProducts.toHalfProductDomain(): HalfProductDomain {
    return HalfProductDomain(
      halfProductId = halfProduct.halfProductId,
      name = halfProduct.name,
      halfProductUnit = halfProduct.halfProductUnit,
      products = products.map { it.toProductDomain() }
    )
  }
}
