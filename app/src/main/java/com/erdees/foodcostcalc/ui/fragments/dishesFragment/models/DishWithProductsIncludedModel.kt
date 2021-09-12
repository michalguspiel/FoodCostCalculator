package com.erdees.foodcostcalc.ui.fragments.dishesFragment.models

import androidx.room.Embedded
import androidx.room.Ignore
import androidx.room.Relation
import com.erdees.foodcostcalc.ui.fragments.productsFragment.models.ProductIncluded
import com.erdees.foodcostcalc.utils.Utils.formatPriceOrWeight
import java.text.NumberFormat

/** TODO REFACTOR */

data class DishWithProductsIncludedModel(
    @Embedded val dishModel: DishModel,
    @Relation(
        parentColumn = "dishId",
        entityColumn = "dishOwnerId"
    )
    val productIncluded: List<ProductIncluded>
) {


    @Ignore
    val totalPrice: Double = productIncluded.map { it.totalPriceOfThisProduct }.sum()

    @Ignore
    val formattedTotalPrice: String = NumberFormat.getCurrencyInstance().format(totalPrice)

    @Ignore
    val priceWithMargin = totalPrice * this.dishModel.marginPercent / 100

    @Ignore
    val formattedPriceWithMargin = formatPriceOrWeight(priceWithMargin)

    @Ignore
    val priceWithMarginAndTax = priceWithMargin + (priceWithMargin * this.dishModel.dishTax / 100)

    @Ignore
    val formattedPriceWithMarginAndTax: String =
        NumberFormat.getCurrencyInstance().format(priceWithMarginAndTax)

    override fun toString(): String {
        return if (productIncluded.isEmpty()) "${dishModel.name} without any ingredients."
        else " ${dishModel.name} includes: " +
                productIncluded.map { it.productModelIncluded.name }.sortedBy { it }
                    .joinToString { "\n-$it" } +
                "\n with total food cost of: $formattedTotalPrice. " +
                "\n Price with calculated margin : $formattedPriceWithMargin." +
                "\n Price with calculated margin and tax : $formattedPriceWithMarginAndTax."
    }
}