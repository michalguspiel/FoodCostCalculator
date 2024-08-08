package com.erdees.foodcostcalc.data.model.joined

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.erdees.foodcostcalc.data.model.HalfProduct
import com.erdees.foodcostcalc.data.model.Product
import com.erdees.foodcostcalc.data.model.ProductHalfProduct

data class HalfProductWithProducts(
  @Embedded val halfProduct: HalfProduct,
  @Relation(
    parentColumn = "halfProductId",
    entityColumn = "productId",
    associateBy = Junction(ProductHalfProduct::class)
  )
  val products: List<Product>
)
