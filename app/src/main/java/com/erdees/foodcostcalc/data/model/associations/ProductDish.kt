package com.erdees.foodcostcalc.data.model.associations

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Keep
@Entity(
  tableName = "Product_Dish",
  indices = [Index("productId")]
)
data class ProductDish(
  @PrimaryKey(autoGenerate = true) val productDishId: Long,
  val productId: Long,
  val dishId: Long,
  val quantity: Double,
  val quantityUnit: String
)
