package com.erdees.foodcostcalc.data.model.joined

import androidx.annotation.Keep
import androidx.room.Embedded
import androidx.room.Ignore
import androidx.room.Junction
import androidx.room.Relation
import com.erdees.foodcostcalc.data.model.Dish
import com.erdees.foodcostcalc.data.model.HalfProduct
import com.erdees.foodcostcalc.data.model.HalfProductDish
import com.erdees.foodcostcalc.data.model.Product
import com.erdees.foodcostcalc.data.model.ProductDish

@Keep
data class CompleteDish(
  @Embedded val dishModel: Dish,
  @Relation(
    parentColumn = "dishId",
    entity = Product::class,
    entityColumn = "productId",
    associateBy = Junction(
      value = ProductDish::class,
      parentColumn = "dishId",
      entityColumn = "dishId"
    )
  )
  val products: List<Product>,
  @Relation(
    parentColumn = "dishId",
    entity = HalfProduct::class,
    entityColumn = "halfProductId",
    associateBy = Junction(
      value = HalfProductDish::class,
      parentColumn = "dishId",
      entityColumn = "dishId"
    )
  )
  val halfProducts: List<HalfProductWithProducts>
)
