package com.example.foodcostcalc.model

import androidx.room.*
import java.math.RoundingMode
import java.text.DecimalFormat

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
                val df = DecimalFormat("#.##")
                df.roundingMode = RoundingMode.CEILING
                val formated = df.format(totalPrice)
                return if(productIncluded.isEmpty()) "${dish.name} without any ingredients."
                else " ${dish.name} includes: " +
                        productIncluded.map { it.productIncluded.name }.sortedBy { it }.joinToString { it } +
                        " with total price of: $formated. "
        }
}




