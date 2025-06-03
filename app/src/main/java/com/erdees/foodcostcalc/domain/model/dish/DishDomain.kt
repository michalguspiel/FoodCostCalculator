package com.erdees.foodcostcalc.domain.model.dish

import android.icu.util.Currency
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
                Timber.v("Product: ${it}, quantity : ${it.quantity}, totalPrice: $totalPrice")
            }
        } + halfProducts.sumOf {
            it.totalPrice.also { totalPrice ->
                Timber.v("Half product: ${it}, quantity : ${it.quantity}, totalPrice: $totalPrice")
            }
        }

    fun formattedFoodCostPerServings(amountOfServings: Int, currency: Currency?): String {
        return Utils.formatPrice(foodCost * amountOfServings, currency)
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

    fun formattedTotalPricePerServing(amountOfServings: Int, currency: Currency?): String {
        return Utils.formatPrice(finalPricePerServing(amountOfServings), currency)
    }

    fun withUpdatedTotalPrice(newTotalPrice: Double): DishDomain {
        val priceBeforeTax = newTotalPrice / (1 + taxPercent / 100)
        val marginAmount = priceBeforeTax - foodCost
        val newMarginPercent = if (foodCost == 0.0) 0.0 else (marginAmount / foodCost) * 100
        return this.copy(marginPercent = newMarginPercent)
    }
}
