package com.erdees.foodcostcalc.data.model.associations

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.erdees.foodcostcalc.data.model.DishBase
import com.erdees.foodcostcalc.data.model.ProductBase

@Keep
@Entity(
  tableName = "Product_Dish",
  indices = [Index("productId")],
  foreignKeys = [
    ForeignKey(
      entity = ProductBase::class,
      parentColumns = ["productId"],
      childColumns = ["productId"],
      onDelete = ForeignKey.CASCADE
    ),
    ForeignKey(
      entity = DishBase::class,
      parentColumns = ["dishId"],
      childColumns = ["dishId"],
      onDelete = ForeignKey.CASCADE
    )
  ]
)
data class ProductDish(
  @PrimaryKey(autoGenerate = true) val productDishId: Long,
  val productId: Long,
  val dishId: Long,
  val quantity: Double,
  val quantityUnit: String
)
