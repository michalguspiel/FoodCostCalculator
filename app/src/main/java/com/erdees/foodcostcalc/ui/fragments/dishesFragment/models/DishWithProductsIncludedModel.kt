package com.erdees.foodcostcalc.ui.fragments.dishesFragment.models

import androidx.room.Embedded
import androidx.room.Relation
import com.erdees.foodcostcalc.ui.fragments.productsFragment.models.ProductIncluded

data class DishWithProductsIncludedModel(
    @Embedded val dishModel: DishModel,
    @Relation(
        parentColumn = "dishId",
        entityColumn = "dishOwnerId"
    )
    val productIncluded: List<ProductIncluded>
)