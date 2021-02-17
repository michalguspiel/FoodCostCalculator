package com.example.foodcostcalc.model

import androidx.room.*
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat


fun formatPriceOrWeight(number: Double):String{
    val df = DecimalFormat("#.##")
    return df.format(number)
}
/**  Its basically a product but with dishOwnerId ref and weight */
@Entity
data class ProductIncluded(@PrimaryKey(autoGenerate = true) val productIncludedId: Long,
                           @Embedded val productIncluded: Product,
                           val dishOwnerId: Long,
                           @Embedded val dish: Dish,
                           val productOwnerId: Long,
                           var weight: Double,
                           val weightUnit: String
) {
    @Ignore
    val unitAbbreviation: String = when(weightUnit){
        "kilogram" -> "kg"
        "gram" -> "g"
        "pound" -> "lb"
        "ounce" -> "oz"
        "liter" -> "l"
        "milliliter" -> "ml"
        "gallon" -> "gal"
        else -> "fl oz"
    }

    @Ignore
    val totalPriceOfThisProduct: Double = this.productIncluded.priceAfterWasteAndTax *
            when (this.productIncluded.unit) {
        "per piece" -> weight
        "per kilogram" -> when (weightUnit) {
            "kilogram" -> weight
            "gram" -> weight / 1000
            "pound" -> (weight / 1000) * 453.59237
            else -> (weight / 1000) * 28.3495
        }
        "per pound" -> when (weightUnit) {
            "kilogram" -> weight / 0.45359237
            "gram" -> weight / 453.59237
            "pound" -> weight
            else -> weight / 16
        }
        "per gallon" -> when (weightUnit) {
            "liter" -> weight / 3.78541178
            "milliliter" -> weight / 3.78541178 / 1000
            "gallon" -> weight
            else -> weight / 128
        }
        "per liter" -> when (weightUnit) {
            "liter" -> weight
            "milliliter" -> weight / 1000
            "gallon" -> weight * 3.78541178
            else -> weight * 0.02957353
        }
        else -> weight

    }

    @Ignore
    val finalFormatPriceOfProduct: String = NumberFormat.getCurrencyInstance().format(totalPriceOfThisProduct.toDouble())
    @Ignore
    val formattedWeightInCaseSomeoneIsCrazy = formatPriceOrWeight(weight)

}
data class DishWithProductsIncluded(
        @Embedded val dish: Dish,
        @Relation(

                parentColumn = "dishId",
                entityColumn = "dishOwnerId"
        )
        val productIncluded: List<ProductIncluded>
) {
    @Ignore
    val totalPrice:Double = productIncluded.map { it.totalPriceOfThisProduct }.sum()
    @Ignore
    val formattedTotalPrice = NumberFormat.getCurrencyInstance().format(totalPrice)
    @Ignore
    val priceWithMargin = totalPrice * this.dish.marginPercent / 100
    @Ignore
    val formattedPriceWithMargin = formatPriceOrWeight(priceWithMargin)
    @Ignore
    val priceWithMarginAndTax = priceWithMargin + (priceWithMargin * this.dish.dishTax / 100)
    @Ignore
    val formattedPriceWithMarginAndTax = NumberFormat.getCurrencyInstance().format(priceWithMarginAndTax)

    override fun toString(): String {
        return if (productIncluded.isEmpty()) "${dish.name} without any ingredients."
        else " ${dish.name} includes: " +
                productIncluded.map { it.productIncluded.name }.sortedBy { it }.joinToString { "\n-$it" } +
                "\n with total food cost of: $formattedTotalPrice. " +
                "\n Price with calculated margin : $formattedPriceWithMargin." +
                "\n Price with calculated margin and tax : $formattedPriceWithMarginAndTax."
    }
}




