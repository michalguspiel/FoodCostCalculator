package com.erdees.foodcostcalc.entities

import androidx.annotation.Keep
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Keep
@Entity(tableName = "HalfProductIncludedInDish")
data class HalfProductIncludedInDish(
  @PrimaryKey(autoGenerate = true) val halfProductIncludedInDishId: Long,
  @Embedded val dish: Dish,
  val dishOwnerId: Long,
  @Embedded val halfProduct: HalfProduct,
  val halfProductOwnerId: Long,
  var weight: Double,
  var unit: String
)
