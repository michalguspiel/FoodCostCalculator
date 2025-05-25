package com.erdees.foodcostcalc.data.model.local.joined

import androidx.room.Embedded
import androidx.room.Relation
import com.erdees.foodcostcalc.data.model.local.HalfProductBase
import com.erdees.foodcostcalc.data.model.local.associations.ProductHalfProduct

data class CompleteHalfProduct(
    @Embedded val halfProductBase: HalfProductBase,
    @Relation(
    parentColumn = "halfProductId",
    entityColumn = "halfProductId",
    entity = ProductHalfProduct::class
  )
  val products: List<ProductUsedInHalfProduct>
)
