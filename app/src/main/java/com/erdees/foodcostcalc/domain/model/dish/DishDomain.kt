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
import kotlin.math.abs

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
    val foodCost: Double = products.sumOf {
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

    @Suppress("MagicNumber")
    fun withUpdatedTotalPrice(newTotalPrice: Double): DishDomain {
        val taxFactor = 1 + taxPercent / PERCENT_MULTIPLIER

        val sellingPriceBeforeTax = newTotalPrice / taxFactor
        val calculatedMarginPercent: Double = if (foodCost == 0.0) {
            if (sellingPriceBeforeTax == 0.0) {
                PERCENT_MULTIPLIER
            } else {
                if (sellingPriceBeforeTax > 0) Double.POSITIVE_INFINITY else Double.NEGATIVE_INFINITY
            }
        } else {
            (sellingPriceBeforeTax / foodCost) * PERCENT_MULTIPLIER
        }

        return adjustMarginWithCorrectDecimals(calculatedMarginPercent, newTotalPrice)
    }

    private fun adjustMarginWithCorrectDecimals(
        marginPercent: Double,
        totalPrice: Double
    ): DishDomain {
        var adjustedDish = this.copy(marginPercent = marginPercent)
        var precision = 1
        for (iterationCount in 0 until MAX_ITERATIONS) {
            val roundedMarginPercent = Utils.formatDouble(precision, marginPercent)
            adjustedDish = this.copy(marginPercent = roundedMarginPercent)

            if (abs(adjustedDish.totalPrice - totalPrice) <= TOLERANCE) {
                Timber.d("Converged: totalPrice=$totalPrice, result.totalPrice=${adjustedDish.totalPrice}, margin=${adjustedDish.marginPercent}, decimals=$precision")
                return adjustedDish
            }
            precision++
        }
        Timber.w(
            "Could not achieve exact total price. " + "Target: $totalPrice, Achieved: ${adjustedDish.totalPrice} with margin ${adjustedDish.marginPercent} at $precision decimal points."
        )
        return adjustedDish
    }

    companion object {
        private const val MAX_ITERATIONS = 5
        private const val TOLERANCE = 0.001
        private const val PERCENT_MULTIPLIER = 100.0
    }
}
