package com.example.foodcostcalc.model

import androidx.room.*
import java.math.RoundingMode
import java.text.DecimalFormat

/**  Its basically a product but with dishOwnerId ref and weight */
@Entity
data class ProductIncluded(@PrimaryKey(autoGenerate = true) val productIncludedId: Long,
                           @Embedded val productIncluded: Product,
                           val dishOwnerId: Long,
                           @Embedded val dish: Dish,
                           val productOwnerId: Long,
                           var weight: Double
){
    override fun toString(): String {
        return "name  ${productIncluded.name} individualid: $productIncludedId ${productIncluded.productId} and the same $productOwnerId"
    }
}

data class DishWithProductsIncluded(
        @Embedded val dish: Dish,
        @Relation(

                parentColumn = "dishId",
                entityColumn = "dishOwnerId"
        )
        val productIncluded: List<ProductIncluded>
) {
    override fun toString(): String {
        val totalPrice: Double = productIncluded.map {
            (it.productIncluded.priceAfterWasteAndTax *
                    when (it.productIncluded.unit) {
                        "per piece" -> it.weight
                        "per kilogram" -> it.weight
                        "per gram" -> it.weight / 1000
                        "per milliliter" -> it.weight / 1000
                        "per liter" -> it.weight
                        else -> it.weight
                    })
        }.sum()
        val priceWithMargin = totalPrice * this.dish.marginPercent / 100
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.CEILING
        val formatted = df.format(totalPrice)
        val formattedFinalPrice = df.format(priceWithMargin)
        return if (productIncluded.isEmpty()) "${dish.name} without any ingredients."
        else " ${dish.name} includes: " +
                productIncluded.map { it.productIncluded.name }.sortedBy { it }.joinToString { "\n-$it" } +
                "\n with total food cost of: $formatted. " +
                "\n price with calculated margin : $formattedFinalPrice"
    }
}




