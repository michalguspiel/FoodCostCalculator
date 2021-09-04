package com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.erdees.foodcostcalc.ui.fragments.productsFragment.models.ProductModel
import com.erdees.foodcostcalc.utils.SharedFunctions.calculatePrice

@Entity(tableName = "ProductIncludedInHalfProduct")
data class ProductIncludedInHalfProductModel(
    @PrimaryKey(autoGenerate = true) val productIncludedInHalfProductId: Long,
    @Embedded val productModelIncluded: ProductModel,
    @Embedded val halfProductModel: HalfProductModel,
    val halfProductHostId: Long,
    var weight: Double,
    val weightUnit: String,
    var weightOfPiece: Double = 1.0
) {
    @Ignore
    val totalWeightForPiece = weight * weightOfPiece

    @Ignore
    val totalPriceOfThisProduct: Double = calculatePrice(
        this.productModelIncluded.priceAfterWasteAndTax,
        this.weight,
        this.productModelIncluded.unit,
        this.weightUnit
    )
}