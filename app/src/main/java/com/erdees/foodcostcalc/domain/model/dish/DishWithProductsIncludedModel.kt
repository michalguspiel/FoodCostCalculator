package com.erdees.foodcostcalc.domain.model.dish

import androidx.room.Embedded
import androidx.room.Relation
import com.erdees.foodcostcalc.domain.model.product.ProductIncluded

data class DishWithProductsIncludedModel(
  @Embedded val dishModel: DishModel,
  @Relation(
        parentColumn = "dishId",
        entityColumn = "dishOwnerId"
    )
    val productIncluded: List<ProductIncluded>
)
