package com.erdees.foodcostcalc.domain.model.dish

import android.icu.util.Currency
import androidx.annotation.Keep
import com.erdees.foodcostcalc.data.repository.AnalyticsRepository
import com.erdees.foodcostcalc.domain.model.Item
import com.erdees.foodcostcalc.domain.model.halfProduct.HalfProductAddedToDish
import com.erdees.foodcostcalc.domain.model.halfProduct.UsedHalfProductDomain
import com.erdees.foodcostcalc.domain.model.product.ProductAddedToDish
import com.erdees.foodcostcalc.domain.model.product.UsedProductDomain
import com.erdees.foodcostcalc.domain.model.recipe.RecipeDomain
import com.erdees.foodcostcalc.utils.Constants
import com.erdees.foodcostcalc.utils.Utils
import kotlinx.serialization.Serializable
import timber.log.Timber
import kotlin.math.abs

/**
 *
 * @param productsNotSaved added to the dish by user but not saved yet.
 * @param halfProductsNotSaved added to the dish by user but not saved yet.
 * */
@Keep
@Serializable
data class DishDomain(
    override val id: Long,
    override val name: String,
    val marginPercent: Double,
    val taxPercent: Double,
    val products: List<UsedProductDomain>,
    val halfProducts: List<UsedHalfProductDomain>,
    val productsNotSaved: List<ProductAddedToDish> = emptyList(),
    val halfProductsNotSaved: List<HalfProductAddedToDish> = emptyList(),
    val recipe: RecipeDomain?,
) : Item {
    val foodCost: Double =
        products.sumOf { it.foodCost } +
                halfProducts.sumOf { it.foodCost } +
                productsNotSaved.sumOf { it.foodCost } +
                halfProductsNotSaved.sumOf { it.foodCost }

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
    fun withUpdatedTotalPrice(newTotalPrice: Double, analyticsRepository: AnalyticsRepository): DishDomain {
        if (foodCost == 0.0) {
            analyticsRepository.logEvent(Constants.Analytics.DishV2.UPDATE_TOTAL_PRICE_ZERO_FOOD_COST)
            return this
        }

        val taxFactor = 1 + taxPercent / PERCENT_MULTIPLIER
        val sellingPriceBeforeTax = newTotalPrice / taxFactor
        val calculatedMarginPercent = (sellingPriceBeforeTax / foodCost) * PERCENT_MULTIPLIER

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
            """
            Failed to converge on target total price within tolerance.
            Target: $targetTotalPrice
            Best achieved: ${bestAttemptDish.totalPrice}
            Difference: $closestDifference (Tolerance: $TOLERANCE)
            Margin used: ${bestAttemptDish.marginPercent}
            Max precision for margin: ${currentPrecision - 1} decimal points.
            """.trimIndent()
        )
        return bestAttemptDish
    }

    companion object {
        private const val MAX_ITERATIONS = 5
        private const val TOLERANCE = 0.001
        private const val PERCENT_MULTIPLIER = 100.0
    }
}
