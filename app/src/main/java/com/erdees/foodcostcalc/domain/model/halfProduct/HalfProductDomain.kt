package com.erdees.foodcostcalc.domain.model.halfProduct

import android.icu.util.Currency
import androidx.annotation.Keep
import com.erdees.foodcostcalc.domain.model.Item
import com.erdees.foodcostcalc.domain.model.product.UsedProductDomain
import com.erdees.foodcostcalc.utils.UnitsUtils.computeWeightAndVolumeToSameUnit
import com.erdees.foodcostcalc.utils.Utils
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class HalfProductDomain(
    override val id: Long,
    override val name: String,
    val halfProductUnit: String,
    val products: List<UsedProductDomain>
) : Item {
    private val singleRecipePrice = products.sumOf { it.totalPrice }

    val totalQuantity =
        if (halfProductUnit == "per piece") 1.0
        else products.sumOf {
            if (it.quantityUnit == "piece") {
                it.weightPiece ?: it.quantity
                computeWeightAndVolumeToSameUnit(
                    halfProductUnit,
                    (halfProductUnit.drop(4)),
                    it.weightPiece?.times(it.quantity) ?: it.quantity
                )
            } else {
                computeWeightAndVolumeToSameUnit(
                    halfProductUnit,
                    it.quantityUnit,
                    it.quantity
                )
            }
        }

    val pricePerUnit: Double
        get() {
            return when (halfProductUnit) {
                "per piece" -> singleRecipePrice
                else -> singleRecipePrice / totalQuantity
            }
        }

    fun formattedSingleRecipePrice(currency: Currency?): String = Utils.formatPrice(singleRecipePrice, currency)

    fun formattedPricePerUnit(currency: Currency?): String = Utils.formatPrice(pricePerUnit, currency)

    fun formattedPricePresentedRecipe(
        baseQuantity: Double,
        targetQuantity: Double,
        currency: Currency?
    ): String {
        val percentageOfBaseQuantity = targetQuantity * 100 / baseQuantity
        val adjustedPrice = singleRecipePrice * (percentageOfBaseQuantity / 100)
        return Utils.formatPrice(adjustedPrice, currency)
    }
}