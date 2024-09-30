package com.erdees.foodcostcalc.data.model.joined

import androidx.annotation.Keep
import androidx.room.Embedded
import androidx.room.Relation
import com.erdees.foodcostcalc.data.model.DishBase
import com.erdees.foodcostcalc.data.model.associations.HalfProductDish
import com.erdees.foodcostcalc.data.model.associations.ProductDish

@Keep
data class CompleteDish(
  @Embedded val dish: DishBase,
  @Relation(
    entity = ProductDish::class,
    parentColumn = "dishId",
    entityColumn = "dishId",
  )
  val products: List<ProductAndProductDish>,
  @Relation(
    parentColumn = "dishId",
    entityColumn = "dishId",
    entity = HalfProductDish::class
  )
  val halfProducts: List<HalfProductUsedInDish>
)
