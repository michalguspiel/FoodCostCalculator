package com.erdees.foodcostcalc.model

import androidx.room.*
import com.erdees.foodcostcalc.computeWeightToSameUnit
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
        else halfProductsList.map {
            if(it.weightUnit != "piece")
                computeWeightToSameUnit(halfProduct.halfProductUnit,it.weightUnit,it.weight)
            else {
                computeWeightToSameUnit(halfProduct.halfProductUnit,(halfProduct.halfProductUnit.drop(4)), it.weightOfPiece * it.weight)
            }
        }.sum()
    }
    fun pricePerUnit(): Double {
        val totalPrice =  halfProductsList.map { it.totalPriceOfThisProduct }.sum()
        return when(halfProduct.halfProductUnit){
            "per piece" -> totalPrice
            "per kilogram" -> totalPrice / totalWeight()
            "per liter" -> totalPrice / totalWeight()
            "per gallon" -> totalPrice / totalWeight()
            "per pound" -> totalPrice / totalWeight()
            else -> 99999.999
        }

    }
    @Ignore
    val formattedPricePerUnit = NumberFormat.getCurrencyInstance().format(pricePerUnit())

}
