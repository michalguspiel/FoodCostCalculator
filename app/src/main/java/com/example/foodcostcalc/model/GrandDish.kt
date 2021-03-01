package com.example.foodcostcalc.model

import androidx.room.Embedded
import androidx.room.Ignore
import androidx.room.Relation
import com.example.foodcostcalc.formatPriceOrWeight
import java.text.NumberFormat

/**Grand dish represents biggest data model in this app,
 * its made of DishWithProductsIncluded which includes: Dish,ProductsIncluded,Products
 * and DishWithHalfProduct which includes: Dish,HalfProduct,ProductsIncludedInHalfProduct,Products,ProductsIncluded*/
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
    @Ignore
    val formattedTotalPrice = NumberFormat.getCurrencyInstance().format(totalPrice)
    @Ignore
    val priceWithMargin = totalPrice * this.dish.marginPercent / 100
    @Ignore
    val formattedPriceWithMargin = formatPriceOrWeight(priceWithMargin)
    @Ignore
    val priceWithMarginAndTax = priceWithMargin + (priceWithMargin * this.dish.dishTax / 100)
    @Ignore
    val formattedPriceWithMarginAndTax = NumberFormat.getCurrencyInstance().format(priceWithMarginAndTax)

}