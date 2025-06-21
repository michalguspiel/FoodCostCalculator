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
        it.foodCost.also { totalPrice ->
            Timber.v("Product: ${it}, quantity : ${it.quantity}, totalPrice: $totalPrice")
        }
    } + halfProducts.sumOf {
        it.foodCost.also { totalPrice ->
            Timber.v("Half product: ${it}, quantity : ${it.quantity}, totalPrice: $totalPrice")
        }
    }

    fun formattedFoodCostPerServings(amountOfServings: Int, currency: Currency?): String {
        return Utils.formatPrice(foodCost * amountOfServings, currency)
    }

    val totalPrice: Double
        get() = Utils.getDishFinalPrice(foodCost, marginPercent, taxPercent)


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
        initialRawMarginPercent: Double,
        targetTotalPrice: Double
    ): DishDomain {
        var currentPrecision = 1
        var bestAttemptDish = this.copy(marginPercent = initialRawMarginPercent)
        var closestDifference = abs(bestAttemptDish.totalPrice - targetTotalPrice)

        repeat(MAX_ITERATIONS) {
            val roundedMarginPercent = Utils.formatDouble(currentPrecision, initialRawMarginPercent)
            val candidateDish = this.copy(marginPercent = roundedMarginPercent)
            val currentDifference = abs(candidateDish.totalPrice - targetTotalPrice)

            if (currentDifference <= TOLERANCE) {
                Timber.d("Converged: targetTotalPrice=$targetTotalPrice, result.totalPrice=${candidateDish.totalPrice}, margin=${candidateDish.marginPercent}, precision=$currentPrecision")
                return candidateDish
            }

            if (currentDifference < closestDifference) {
                closestDifference = currentDifference
                bestAttemptDish = candidateDish
                Timber.v("New best attempt: totalPrice=${candidateDish.totalPrice}, margin=${candidateDish.marginPercent}, precision=$currentPrecision, diff=$currentDifference")
            }
            currentPrecision++
        }

        Timber.w(
            "Could not achieve exact total price within TOLERANCE. " +
                    "Target: $targetTotalPrice, Best Achieved: ${bestAttemptDish.totalPrice} " +
                    "with margin ${bestAttemptDish.marginPercent} (tried up to ${currentPrecision - 1} decimal points for margin)."
        )
        return bestAttemptDish
    }
    companion object {
        private const val MAX_ITERATIONS = 5
        private const val TOLERANCE = 0.001
        private const val PERCENT_MULTIPLIER = 100.0
    }
}
