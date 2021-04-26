package com.erdees.foodcostcalc.model

import androidx.room.Embedded
import androidx.room.Ignore
import androidx.room.Relation
import com.erdees.foodcostcalc.SharedFunctions.formatPriceOrWeight
import java.text.NumberFormat

    data class DishWithProductsIncluded(
        @Embedded val dish: Dish,
        @Relation(
            parentColumn = "dishId",
            entityColumn = "dishOwnerId"
        )
        val productIncluded: List<ProductIncluded>
    ) {


        @Ignore
        val totalPrice:Double = productIncluded.map { it.totalPriceOfThisProduct }.sum()
        @Ignore
        val formattedTotalPrice: String = NumberFormat.getCurrencyInstance().format(totalPrice)
        @Ignore
        val priceWithMargin = totalPrice * this.dish.marginPercent / 100
        @Ignore
        val formattedPriceWithMargin = formatPriceOrWeight(priceWithMargin)
        @Ignore
        val priceWithMarginAndTax = priceWithMargin + (priceWithMargin * this.dish.dishTax / 100)
        @Ignore
        val formattedPriceWithMarginAndTax: String = NumberFormat.getCurrencyInstance().format(priceWithMarginAndTax)

        override fun toString(): String {
            return if (productIncluded.isEmpty()) "${dish.name} without any ingredients."
            else " ${dish.name} includes: " +
                    productIncluded.map { it.productIncluded.name }.sortedBy { it }.joinToString { "\n-$it" } +
                    "\n with total food cost of: $formattedTotalPrice. " +
                    "\n Price with calculated margin : $formattedPriceWithMargin." +
                    "\n Price with calculated margin and tax : $formattedPriceWithMarginAndTax."
        }
    }