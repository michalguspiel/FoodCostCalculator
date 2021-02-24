package com.example.foodcostcalc.model

import androidx.room.Embedded
import androidx.room.Ignore
import androidx.room.Relation
import java.text.NumberFormat

data class HalfProductWithProductsIncluded (
    @Embedded val halfProduct: HalfProduct,
    @Relation(
        parentColumn = "halfProductId",
        entityColumn = "halfProductOwnerId"
    )
    val productIncludedInHalfProduct: List<ProductIncludedInHalfProduct>

        ){
    @Ignore
    val totalPricePerUnit = productIncludedInHalfProduct.map { it.totalPriceOfThisProduct}.sum()

    @Ignore
    val formattedTotalPrice = NumberFormat.getCurrencyInstance().format(totalPricePerUnit)

}