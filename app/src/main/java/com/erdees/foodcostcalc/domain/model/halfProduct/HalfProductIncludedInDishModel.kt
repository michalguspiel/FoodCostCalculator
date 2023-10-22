package com.erdees.foodcostcalc.domain.model.halfProduct

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.erdees.foodcostcalc.domain.model.dish.DishModel

@Entity(tableName = "HalfProductIncludedInDish")
data class HalfProductIncludedInDishModel(
  @PrimaryKey(autoGenerate = true) val halfProductIncludedInDishId: Long,
  @Embedded val dishModel: DishModel,
  val dishOwnerId: Long,
  @Embedded val halfProductModel: HalfProductModel,
  val halfProductOwnerId: Long,
  var weight: Double,
  var unit: String

) {

}
