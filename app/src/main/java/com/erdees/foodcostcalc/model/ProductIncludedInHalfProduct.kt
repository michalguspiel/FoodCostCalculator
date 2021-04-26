package com.erdees.foodcostcalc.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.erdees.foodcostcalc.SharedFunctions.calculatePrice
import com.erdees.foodcostcalc.SharedFunctions.formatPriceOrWeight
import java.text.NumberFormat

@Entity
data class ProductIncludedInHalfProduct(
    @PrimaryKey(autoGenerate = true) val productIncludedInHalfProductId: Long,
    @Embedded val productIncluded: Product,
    @Embedded val halfProduct: HalfProduct,
    val halfProductHostId : Long,
    var weight: Double,
    val weightUnit: String,
    var weightOfPiece: Double = 1.0
) {

    @Ignore
    val totalWeightForPiece = weight * weightOfPiece

    @Ignore
    val totalPriceOfThisProduct: Double = calculatePrice(this.productIncluded.priceAfterWasteAndTax,this.weight,this.productIncluded.unit,this.weightUnit)

    @Ignore
    val finalFormatPriceOfProduct: String = NumberFormat.getCurrencyInstance().format(
        totalPriceOfThisProduct
    )
    @Ignore
    val formattedWeight = formatPriceOrWeight(weight)
}