package com.erdees.foodcostcalc.domain.model.dish

import androidx.annotation.Keep
import androidx.room.Embedded
import androidx.room.Ignore
import androidx.room.Relation
import com.erdees.foodcostcalc.entities.HalfProductIncludedInDish
import com.erdees.foodcostcalc.entities.ProductIncluded
import com.erdees.foodcostcalc.entities.Dish

/**[GrandDish] is a dish which contains products and half products]*/
@Keep
data class GrandDish(
  @Embedded val dish: Dish,
  @Relation(
        parentColumn = "dishId",
        entityColumn = "dishOwnerId"
    )
    val productsIncluded: List<ProductIncluded>,
  @Relation(
        parentColumn = "dishId",
        entityColumn = "dishOwnerId"
    )
    val halfProducts: List<HalfProductIncludedInDish>
) {
    @Ignore
    val totalPrice: Double = productsIncluded.map { it.totalPriceOfThisProduct }.sum()

}
