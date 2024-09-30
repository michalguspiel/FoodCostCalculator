package com.erdees.foodcostcalc.data.model.joined

import androidx.room.Embedded
import androidx.room.Relation
import com.erdees.foodcostcalc.data.model.ProductBase
import com.erdees.foodcostcalc.data.model.associations.ProductHalfProduct

data class ProductUsedInHalfProduct(
  @Embedded val productHalfProduct: ProductHalfProduct,
  @Relation(
    parentColumn = "productId",
    entityColumn = "productId",
    entity = ProductBase::class
  )
  val product: ProductBase
)
