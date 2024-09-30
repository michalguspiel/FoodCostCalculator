package com.erdees.foodcostcalc.data.model.joined

import androidx.annotation.Keep
import androidx.room.Embedded
import androidx.room.Relation
import com.erdees.foodcostcalc.data.model.ProductBase
import com.erdees.foodcostcalc.data.model.associations.ProductDish

@Keep
data class ProductAndProductDish(
  @Embedded val productDish: ProductDish,
  @Relation(
    parentColumn = "productId",
    entityColumn = "productId",
    entity = ProductBase::class
  )
  val product: ProductBase,
)
