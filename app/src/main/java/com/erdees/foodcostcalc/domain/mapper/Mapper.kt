package com.erdees.foodcostcalc.domain.mapper

import com.erdees.foodcostcalc.data.model.HalfProductBase
import com.erdees.foodcostcalc.data.model.ProductBase
import com.erdees.foodcostcalc.data.model.joined.CompleteDish
import com.erdees.foodcostcalc.data.model.joined.HalfProductUsedInDish
import com.erdees.foodcostcalc.data.model.joined.CompleteHalfProduct
import com.erdees.foodcostcalc.data.model.joined.ProductAndProductDish
import com.erdees.foodcostcalc.data.model.joined.ProductUsedInHalfProduct
import com.erdees.foodcostcalc.domain.model.DishDomain
import com.erdees.foodcostcalc.domain.model.HalfProductDomain
import com.erdees.foodcostcalc.domain.model.ProductDomain
import com.erdees.foodcostcalc.domain.model.UsedHalfProductDomain
import com.erdees.foodcostcalc.domain.model.UsedProductDomain

object Mapper {
  // TODO fix
  fun CompleteDish.toDishDomain(): DishDomain {
    return DishDomain(
      dishId = dish.dishId,
      name = dish.name,
      marginPercent = dish.marginPercent,
      dishTax = dish.dishTax,
      products = products.map { it.toUsedProductDomain() },
      halfProducts = halfProducts.map { it.toUsedHalfProductDomain()  }
    )
  }

  fun ProductBase.toProductDomain(): ProductDomain {
    return ProductDomain(
      id = productId,
      name = name,
      pricePerUnit = pricePerUnit,
      tax = tax,
      waste = waste,
      unit = unit
    )
  }

  fun HalfProductBase.toHalfProductDomain(): HalfProductDomain {
    return HalfProductDomain(
      id = halfProductId,
      name = name,
      halfProductUnit = halfProductUnit,
      products = emptyList()
    )
  }


  fun ProductBase.toUsedProductDomain(
    quantity: Double,
    quantityUnit: String,
    weightPiece: Double?
  ): UsedProductDomain {
    return UsedProductDomain(
      product = toProductDomain(),
      quantity = quantity,
      quantityUnit = quantityUnit,
      weightPiece = weightPiece
    )
  }

  fun ProductAndProductDish.toUsedProductDomain(): UsedProductDomain {
    return UsedProductDomain(
      product = product.toProductDomain(),
      quantity = productDish.quantity,
      quantityUnit = productDish.quantityUnit,
      weightPiece = null
    )
  }

  fun ProductUsedInHalfProduct.toUsedProductDomain(): UsedProductDomain {
    return UsedProductDomain(
      product = product.toProductDomain(),
      quantity = productHalfProduct.quantity,
      quantityUnit = productHalfProduct.quantityUnit,
      weightPiece = productHalfProduct.weightPiece
    )
  }


  fun CompleteHalfProduct.toHalfProductDomain(): HalfProductDomain {
    return HalfProductDomain(
      id = halfProductBase.halfProductId,
      name = halfProductBase.name,
      halfProductUnit = halfProductBase.halfProductUnit,
      products = products.map { it.toUsedProductDomain() }
    )
  }

  fun HalfProductUsedInDish.toUsedHalfProductDomain(): UsedHalfProductDomain {
    return UsedHalfProductDomain(
      halfProductDomain = halfProductsWithProductsBase.toHalfProductDomain(),
      quantity = halfProductDish.quantity,
      quantityUnit = halfProductDish.quantityUnit
    )
  }
}
