package com.erdees.foodcostcalc.ui.fragments.dishesFragment.models

import androidx.room.Embedded
import androidx.room.Ignore
import androidx.room.Relation
import com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.models.HalfProductIncludedInDishModel
import com.erdees.foodcostcalc.ui.fragments.productsFragment.models.ProductIncluded

/**[GrandDishModel] is a dish which contains products and half products]*/
data class GrandDishModel(
    @Embedded val dishModel: DishModel,
    @Relation(
        parentColumn = "dishId",
        entityColumn = "dishOwnerId"
    )
    val productsIncluded: List<ProductIncluded>,
    @Relation(
        parentColumn = "dishId",
        entityColumn = "dishOwnerId"
    )
    val halfProducts: List<HalfProductIncludedInDishModel>
) {
    @Ignore
    val totalPrice: Double = productsIncluded.map { it.totalPriceOfThisProduct }.sum()

}