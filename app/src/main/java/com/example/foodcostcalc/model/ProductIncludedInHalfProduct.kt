package com.example.foodcostcalc.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.example.foodcostcalc.formatPriceOrWeight
import java.text.NumberFormat

@Entity
data class ProductIncludedInHalfProduct(
    @PrimaryKey(autoGenerate = true) val productIncludedInHalfProductId: Long,
    @Embedded val productIncluded: Product,
    val halfProductOwnerId: Long,
    @Embedded val halfProduct: HalfProduct,
    val productOwnerId: Long,
    var weight: Double,
    val weightUnit: String
) {

    @Ignore
    val unitAbbreviation: String = when(weightUnit){
        "piece" -> "pce"
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
    val finalFormatPriceOfProduct: String = NumberFormat.getCurrencyInstance().format(
        totalPriceOfThisProduct
    )
    @Ignore
    val formattedWeightInCaseSomeoneIsCrazy = formatPriceOrWeight(weight)
}