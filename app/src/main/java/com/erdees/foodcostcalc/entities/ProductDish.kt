package com.erdees.foodcostcalc.entities

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

@Keep
@Entity(tableName = "Product_Dish")
data class ProductDish(
  @PrimaryKey(autoGenerate = true) val productDishId: Long,
  val productId: Long,
  val dishId: Long,
  val quantity: Double,
  val quantityUnit: String
)
