package com.erdees.foodcostcalc.domain.model.dish

import android.content.Context
import android.util.Log
import androidx.annotation.Keep
import com.erdees.foodcostcalc.domain.model.Item
import com.erdees.foodcostcalc.domain.model.halfProduct.UsedHalfProductDomain
import com.erdees.foodcostcalc.domain.model.product.UsedProductDomain
import com.erdees.foodcostcalc.utils.Utils
import kotlinx.serialization.Serializable


@Keep
@Serializable
data class DishDomain(
    override val id: Long,
    override val name: String,
    val marginPercent: Double,
    val taxPercent: Double,
    val products: List<UsedProductDomain>,
    val halfProducts: List<UsedHalfProductDomain>
) : Item {
    val foodCost: Double =
        products.sumOf {
            it.totalPrice.also { totalPrice ->
                Log.i(
                    "DishDomain",
                    "Product: ${it}, quantity : ${it.quantity}, totalPrice: $totalPrice"
                )
            }
        } + halfProducts.sumOf {
            it.totalPrice.also { totalPrice ->
                Log.i(
                    "DishDomain",
                    "Half product: ${it}, quantity : ${it.quantity}, totalPrice: $totalPrice"
                )
            }
        }

    fun formattedFoodCost(context: Context): String {
        return Utils.formatPrice(foodCost, context)
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
