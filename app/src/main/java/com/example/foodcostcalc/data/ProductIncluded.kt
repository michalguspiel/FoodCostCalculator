package com.example.foodcostcalc.data

import androidx.room.*
import com.example.foodcostcalc.model.Dish
import com.example.foodcostcalc.model.Product

@Entity
data class ProductIncluded(
    @PrimaryKey(autoGenerate = true) val productIncludedId: Long,
    val dishOwnerId: Long,
    val productOwnerId: Long,
    val weight: Double

){
        override fun toString(): String {
                return super.toString()
        }
}


data class DishWithProductsIncluded(
        @Embedded val dish: Dish,
        @Relation(
                parentColumn = "dishId",
                entityColumn = "dishOwnerId"
        )
        val productIncluded: List<ProductIncluded>
){
        override fun toString(): String {
return " ${dish.name} includes: ${productIncluded.map { it.dishOwnerId }.joinToString { "," }} "
        }
}





