package com.erdees.foodcostcalc.model

import androidx.room.*
import com.erdees.foodcostcalc.calculatePrice
import com.erdees.foodcostcalc.formatPriceOrWeight
import java.text.NumberFormat

/**  Its a product but with dishOwnerId reference and weight */
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
    val totalPriceOfThisProduct: Double
    = calculatePrice(this.productIncluded.priceAfterWasteAndTax,this.weight,this.productIncluded.unit,this.weightUnit)
    @Ignore
    val finalFormatPriceOfProduct: String = NumberFormat.getCurrencyInstance().format(totalPriceOfThisProduct)
    @Ignore
    val formattedWeight = formatPriceOrWeight(weight)

}





