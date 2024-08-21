package com.erdees.foodcostcalc.data.model.joined

import androidx.room.Embedded
import androidx.room.Relation
import com.erdees.foodcostcalc.data.model.HalfProductBase
import com.erdees.foodcostcalc.data.model.associations.ProductHalfProduct

data class CompleteHalfProduct(
  @Embedded val halfProductBase: HalfProductBase,
  @Relation(
    parentColumn = "halfProductId",
    entityColumn = "halfProductId",
    entity = ProductHalfProduct::class
  )
  val products: List<ProductUsedInHalfProduct>
)
