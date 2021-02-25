package com.example.foodcostcalc.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

/**Its half product but with products included in it and reference to dish owner*/
@Entity
data class HalfProductIncludedInDish(
    @PrimaryKey(autoGenerate = true) val halfProductIncludedInDishId: Long,
    @Embedded val halfProductWithProductsIncluded : HalfProductWithProductsIncluded,
    val mainDishOwnerId: Long,
    @Embedded val dish : Dish,
    val halfProductWithProductsIncludedId: Long,
    var weight: Double,
    var unit: String
) {
}