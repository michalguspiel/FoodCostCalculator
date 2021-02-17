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
                           var weight: Double,
                           val weightUnit: String
)

data class DishWithProductsIncluded(
        @Embedded val dish: Dish,
        @Relation(

                parentColumn = "dishId",
                entityColumn = "dishOwnerId"
        )
        val productIncluded: List<ProductIncluded>
) {
    @Ignore /**Basic converter, so far its in here in future I'd like to make separate class for it or something. */
    val totalPrice: Double = productIncluded.map {
        (it.productIncluded.priceAfterWasteAndTax *
                when (it.productIncluded.unit) {
                    "per piece" -> it.weight
                    "per kilogram"   -> when (it.weightUnit) {
                        "kilogram"   -> it.weight
                        "gram"       -> it.weight / 1000
                        "pound"      -> (it.weight / 1000) * 453.59237
                        else         -> (it.weight / 1000) * 28.3495
                    }
                    "per pound" -> when(it.weightUnit){
                        "kilogram"   -> it.weight / 0.45359237
                        "gram"       -> it.weight / 453.59237
                        "pound"      -> it.weight
                        else         -> it.weight / 16
                    }
                    "per gallon" ->  when(it.weightUnit){
                        "liter"      -> it.weight / 3.78541178
                        "milliliter" -> it.weight / 3.78541178 / 1000
                        "gallon"     -> it.weight
                        else         -> it.weight / 128
                    }
                    "per liter" ->  when(it.weightUnit){
                        "liter"      -> it.weight
                        "milliliter" -> it.weight / 1000
                        "gallon"     -> it.weight * 3.78541178
                        else         -> it.weight * 0.02957353
                    }
                    else -> it.weight
                })
    }.sum()

    @Ignore
    val priceWithMargin = totalPrice * this.dish.marginPercent / 100
    @Ignore
    val priceWithMarginAndTax = priceWithMargin + (priceWithMargin * this.dish.dishTax / 100)

    override fun toString(): String {
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.CEILING
        val formatted = df.format(totalPrice)
        val formattedFinalPrice = df.format(priceWithMargin)
        val formattedFinalBruttoPrice = df.format(priceWithMarginAndTax)
        return if (productIncluded.isEmpty()) "${dish.name} without any ingredients."
        else " ${dish.name} includes: " +
                productIncluded.map { it.productIncluded.name }.sortedBy { it }.joinToString { "\n-$it" } +
                "\n with total food cost of: $formatted. " +
                "\n Price with calculated margin : $formattedFinalPrice." +
                "\n Price with calculated margin and tax : $formattedFinalBruttoPrice."
    }
}




