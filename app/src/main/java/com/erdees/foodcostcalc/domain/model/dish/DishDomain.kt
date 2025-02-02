package com.erdees.foodcostcalc.domain.model.dish

import android.content.Context
import androidx.annotation.Keep
import com.erdees.foodcostcalc.domain.model.Item
import com.erdees.foodcostcalc.domain.model.halfProduct.UsedHalfProductDomain
import com.erdees.foodcostcalc.domain.model.product.UsedProductDomain
import com.erdees.foodcostcalc.domain.model.recipe.RecipeDomain
import com.erdees.foodcostcalc.utils.Utils
import kotlinx.serialization.Serializable
import timber.log.Timber


@Keep
@Serializable
data class DishDomain(
    override val id: Long,
    override val name: String,
    val marginPercent: Double,
    val taxPercent: Double,
    val products: List<UsedProductDomain>,
    val halfProducts: List<UsedHalfProductDomain>,
    val recipe: RecipeDomain?,
) : Item {
    val foodCost: Double =
        products.sumOf {
            it.totalPrice.also { totalPrice ->
                Timber.i("Product: ${it}, quantity : ${it.quantity}, totalPrice: $totalPrice")
            }
        } + halfProducts.sumOf {
            it.totalPrice.also { totalPrice ->
                Timber.i("Half product: ${it}, quantity : ${it.quantity}, totalPrice: $totalPrice")
            }
        }

    fun formattedFoodCostPerServings(context: Context, amountOfServings: Int): String {
        return Utils.formatPrice(foodCost * amountOfServings, context)
    }

    val totalPrice: Double
        get() {
            val priceWithMargin = foodCost * marginPercent / 100
            val amountOfTax = priceWithMargin * taxPercent / 100
            return priceWithMargin + amountOfTax
        }

    private fun finalPricePerServing(amountOfServings: Int): Double {
        return totalPrice * amountOfServings
    }

    fun formattedTotalPricePerServing(context: Context, amountOfServings: Int): String {
        return Utils.formatPrice(finalPricePerServing(amountOfServings), context)
    }
}
