package com.erdees.foodcostcalc.data.model.associations

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

@Keep
@Entity(tableName = "HalfProduct_Dish")
data class HalfProductDish(
  @PrimaryKey(autoGenerate = true) val halfProductDishId: Long,
  val halfProductId: Long,
  val dishId: Long,
  val quantity: Double,
  val quantityUnit: String
)
