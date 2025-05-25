package com.erdees.foodcostcalc.data.model.local.joined

import androidx.annotation.Keep
import androidx.room.Embedded
import androidx.room.Relation
import com.erdees.foodcostcalc.data.model.local.DishBase
import com.erdees.foodcostcalc.data.model.local.Recipe
import com.erdees.foodcostcalc.data.model.local.associations.HalfProductDish
import com.erdees.foodcostcalc.data.model.local.associations.ProductDish

@Keep
data class CompleteDish(
    @Embedded val dish: DishBase,
    @Relation(
    entity = Recipe::class,
    parentColumn = "recipeId",
    entityColumn = "recipeId"
  )
  val recipe: RecipeWithSteps?,
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
