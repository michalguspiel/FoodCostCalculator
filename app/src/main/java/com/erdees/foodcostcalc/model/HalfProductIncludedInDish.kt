package com.erdees.foodcostcalc.model

import androidx.room.*

@Entity
data class HalfProductIncludedInDish(
    @PrimaryKey(autoGenerate = true) val halfProductIncludedInDishId: Long,
    @Embedded val dish: Dish,
    val dishOwnerId: Long,
    @Embedded val halfProduct: HalfProduct,
    val halfProductOwnerId: Long,
    var weight: Double,
    var unit: String

) {

}