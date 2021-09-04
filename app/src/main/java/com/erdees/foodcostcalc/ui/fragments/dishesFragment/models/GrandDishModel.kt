package com.erdees.foodcostcalc.ui.fragments.dishesFragment.models

import androidx.room.Embedded
import androidx.room.Ignore
import androidx.room.Relation
import com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.models.HalfProductIncludedInDishModel
import com.erdees.foodcostcalc.ui.fragments.productsFragment.models.ProductIncluded

/**Grand dishModel represents biggest data model in this app,
 * its made of DishWithProductsIncludedModel which includes: DishModel,ProductsIncluded,ProductsFragment
 * and DishWithHalfProduct which includes: DishModel,HalfProductModel,ProductsIncludedInHalfProduct,ProductsFragment,ProductsIncluded
 *
 * So You can basically think of it as DishModel which has products but also half products*/
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
    val halfProductModels: List<HalfProductIncludedInDishModel>
) {
    @Ignore
    val totalPrice: Double = productsIncluded.map { it.totalPriceOfThisProduct }.sum()

}