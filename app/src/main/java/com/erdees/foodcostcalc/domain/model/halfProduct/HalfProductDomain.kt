package com.erdees.foodcostcalc.domain.model.halfProduct

import android.icu.util.Currency
import androidx.annotation.Keep
import com.erdees.foodcostcalc.domain.model.Item
import com.erdees.foodcostcalc.domain.model.product.UsedProductDomain
import com.erdees.foodcostcalc.domain.model.units.MeasurementUnit
import com.erdees.foodcostcalc.utils.Utils
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class HalfProductDomain(
    override val id: Long,
    override val name: String,
    val halfProductUnit: MeasurementUnit,
    val products: List<UsedProductDomain>,
) : Item {
    private val singleRecipePrice = products.sumOf { it.foodCost }

    val totalQuantity =
        if (halfProductUnit == MeasurementUnit.PIECE) 1.0
        else products.sumOf {
            if (it.quantityUnit == MeasurementUnit.PIECE) {
                halfProductUnit.computePricingEquivalent(
                    halfProductUnit,
                    it.weightPiece?.times(it.quantity) ?: it.quantity
                )
            } else {
                halfProductUnit.computePricingEquivalent(it.quantityUnit, it.quantity)
            }
        }

    val pricePerUnit: Double
        get() {
            return when (halfProductUnit) {
                MeasurementUnit.PIECE -> singleRecipePrice
                else -> singleRecipePrice / totalQuantity
            }
        }

    fun formattedSingleRecipePrice(currency: Currency?): String =
        Utils.formatPrice(singleRecipePrice, currency)

    fun formattedPricePerUnit(currency: Currency?): String =
        Utils.formatPrice(pricePerUnit, currency)

    fun formattedPricePresentedRecipe(
        baseQuantity: Double,
        targetQuantity: Double,
        currency: Currency?,
    ): String {
        val percentageOfBaseQuantity = targetQuantity * 100 / baseQuantity
        val adjustedPrice = singleRecipePrice * (percentageOfBaseQuantity / 100)
        return Utils.formatPrice(adjustedPrice, currency)
    }
}