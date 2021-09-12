package com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.models

import androidx.room.Embedded
import androidx.room.Ignore
import androidx.room.Relation
import com.erdees.foodcostcalc.utils.UnitsUtils.computeWeightAndVolumeToSameUnit
import java.text.NumberFormat

/**TODO REFACTOR*/

data class HalfProductWithProductsIncludedModel(
    @Embedded val halfProductModel: HalfProductModel,
    @Relation(
        parentColumn = "halfProductId",
        entityColumn = "halfProductHostId"
    )
    val halfProductsListModel: List<ProductIncludedInHalfProductModel>
) {
    fun totalWeight(): Double {
        return if (halfProductModel.halfProductUnit == "per piece") 1.0
        else halfProductsListModel.map {
            if (it.weightUnit != "piece")
                computeWeightAndVolumeToSameUnit(
                    halfProductModel.halfProductUnit,
                    it.weightUnit,
                    it.weight
                )
            else {
                computeWeightAndVolumeToSameUnit(
                    halfProductModel.halfProductUnit,
                    (halfProductModel.halfProductUnit.drop(4)),
                    it.weightOfPiece * it.weight
                )
            }
        }.sum()
    }

    fun pricePerRecipe(): Double {
        return halfProductsListModel.map { it.totalPriceOfThisProduct }.sum()
    }

    fun pricePerUnit(): Double {
        val totalPrice = halfProductsListModel.map { it.totalPriceOfThisProduct }.sum()
        return when (halfProductModel.halfProductUnit) {
            "per piece" -> totalPrice
            "per kilogram" -> totalPrice / totalWeight()
            "per liter" -> totalPrice / totalWeight()
            "per gallon" -> totalPrice / totalWeight()
            "per pound" -> totalPrice / totalWeight()
            else -> 99999.999
        }

    }

    @Ignore
    val formattedPricePerUnit: String = NumberFormat.getCurrencyInstance().format(pricePerUnit())

    @Ignore
    val formattedPricePerRecipe : String = NumberFormat.getCurrencyInstance().format(pricePerRecipe())
}

