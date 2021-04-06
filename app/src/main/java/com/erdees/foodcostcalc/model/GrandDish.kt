package com.erdees.foodcostcalc.model

import androidx.room.Embedded
import androidx.room.Ignore
import androidx.room.Relation
import com.erdees.foodcostcalc.formatPriceOrWeight
import java.text.NumberFormat

/**Grand dish represents biggest data model in this app,
 * its made of DishWithProductsIncluded which includes: Dish,ProductsIncluded,Products
 * and DishWithHalfProduct which includes: Dish,HalfProduct,ProductsIncludedInHalfProduct,Products,ProductsIncluded
 *
 * So You can basically think of it as Dish which has products but also half products*/
data class GrandDish (
    @Embedded val dish : Dish,
    @Relation(parentColumn = "dishId",
                entityColumn = "dishOwnerId")
     val productsIncluded : List<ProductIncluded>,
    @Relation(parentColumn = "dishId",
        entityColumn = "dishOwnerId")
    val halfProducts: List<HalfProductIncludedInDish>
){
    @Ignore
    val totalPrice:Double = productsIncluded.map { it.totalPriceOfThisProduct }.sum()

}