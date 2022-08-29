package com.erdees.foodcostcalc.domain.model.dish

import androidx.room.Embedded
import androidx.room.Ignore
import androidx.room.Relation
import com.erdees.foodcostcalc.domain.model.halfProduct.HalfProductIncludedInDishModel
import com.erdees.foodcostcalc.domain.model.product.ProductIncluded

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
