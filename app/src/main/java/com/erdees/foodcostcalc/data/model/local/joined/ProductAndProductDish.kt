package com.erdees.foodcostcalc.data.model.local.joined

import androidx.annotation.Keep
import androidx.room.Embedded
import androidx.room.Relation
import com.erdees.foodcostcalc.data.model.local.ProductBase
import com.erdees.foodcostcalc.data.model.local.associations.ProductDish

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
