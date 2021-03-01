package com.example.foodcostcalc.model

import androidx.room.*
import java.text.NumberFormat

data class HalfProductWithProductsIncluded(
    @Embedded val halfProduct: HalfProduct,
    @Relation(
        parentColumn = "halfProductId",
        entityColumn = "halfProductHostId"
    )
    val halfProductsList: List<ProductIncludedInHalfProduct>
) {
    fun totalWeight(): Double {
        return if (halfProduct.halfProductUnit == "per piece") 1.0
        else halfProductsList.map { it.weight }.sum()
    }
    fun pricePerUnit(): Double {    //TODO Functions that compute price per unit and total weight
        return if (halfProduct.halfProductUnit == "per piece") halfProductsList.map { it.totalPriceOfThisProduct }.sum()
        else {
            val totalPrice = halfProductsList.map { it.totalPriceOfThisProduct }.sum()
            totalPrice * 1000 / totalWeight()
        }
    }
    @Ignore
    val formattedPricePerUnit = NumberFormat.getCurrencyInstance().format(pricePerUnit())

}
