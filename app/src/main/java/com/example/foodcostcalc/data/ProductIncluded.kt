package com.example.foodcostcalc.data

import androidx.room.*
import com.example.foodcostcalc.model.Dish
import com.example.foodcostcalc.model.Product

/**  Its basically a product but with dishOwnerId ref and weight */
@Entity
data class ProductIncluded(@PrimaryKey(autoGenerate = true) val productIncludedId : Long,
                           @Embedded val productIncluded: Product,
                           val dishOwnerId: Long,
                           val productOwnerId: Long,
                           var weight: Double

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
                val totalPrice: Double = productIncluded.map {(it.productIncluded.priceAfterWasteAndTax * it.weight)}.sum()
                if(productIncluded.isEmpty()) return "${dish.name} without any ingredients."
                else return " ${dish.name} includes: " +
                        productIncluded.map { it.productIncluded.name }.joinToString { it } +
                        " with total price of: $totalPrice. "
        }
}

/**
@Entity
data class Ingredient(
        val dish: Dish,
        val productOwnerId: Long,
        val productName: String,
        val weight: Double
)
*/


