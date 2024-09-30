package com.erdees.foodcostcalc.data.model.joined

import androidx.room.Embedded
import androidx.room.Relation
import com.erdees.foodcostcalc.data.model.HalfProductBase
import com.erdees.foodcostcalc.data.model.associations.HalfProductDish

data class HalfProductUsedInDish(
    @Embedded val halfProductDish: HalfProductDish,
    @Relation(
    parentColumn = "halfProductId",
    entityColumn = "halfProductId",
    entity = HalfProductBase::class
  )
  val halfProductsWithProductsBase: CompleteHalfProduct
)
