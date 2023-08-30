package com.erdees.foodcostcalc.ui.fragments.dishesFragment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.erdees.foodcostcalc.data.AppRoomDataBase
import com.erdees.foodcostcalc.data.grandDish.GrandDishRepository
import com.erdees.foodcostcalc.data.halfProductWithProductsIncluded.HalfProductWithProductsIncludedRepository
import com.erdees.foodcostcalc.utils.UnitsUtils.calculatePrice
import com.erdees.foodcostcalc.utils.Utils.formatPrice

class DishRVAdapterViewModel(application: Application) : AndroidViewModel(application) {

    private val halfProductWithProductsIncludedRepository: HalfProductWithProductsIncludedRepository
    private val grandDishRepository: GrandDishRepository

    init {
        val halfProductWithProductsIncludedDao =
            AppRoomDataBase.getDatabase(application).halfProductWithProductsIncludedDao()
        val grandDishDao = AppRoomDataBase.getDatabase(application).grandDishDao()
        halfProductWithProductsIncludedRepository =
            HalfProductWithProductsIncludedRepository(halfProductWithProductsIncludedDao)
        grandDishRepository = GrandDishRepository(grandDishDao)
    }

    fun getCertainHalfProductWithProductsIncluded(halfProductId: Long) =
        halfProductWithProductsIncludedRepository.getCertainHalfProductWithProductsIncluded(
            halfProductId
        )

  fun formattedPriceData(dishModelId: Long, amountOfServings: Int): String {
        return formatPrice(getDishData(dishModelId).totalPrice * amountOfServings)
    }

    fun formattedTotalPriceData(dishModelId: Long, amountOfServings: Int): String {
        val dishData = getDishData(dishModelId)
        return formattedTotalPriceData(
            dishData.totalPrice,
            dishData.margin,
            dishData.tax,
            amountOfServings
        )
    }

    private fun formattedTotalPriceData(
        totalPrice: Double,
        dishMargin: Double,
        dishTax: Double,
        amountOfServings: Int
    ): String {
        return formatPrice(
            priceAfterMarginAndTax(
                totalPrice,
                dishMargin,
                dishTax,
                amountOfServings
            )
        )
    }

    private fun priceAfterMarginAndTax(
        totalPrice: Double,
        margin: Double,
        tax: Double,
        amountOfServings: Int
    ): Double {
        val priceWithMargin = totalPrice * margin / 100
        val amountOfTax = priceWithMargin * tax / 100
        return (priceWithMargin + amountOfTax) * amountOfServings
    }

    fun addToTotalPrice(
        dishModelId: Long, pricePerUnit: Double,
        weight: Double,
        halfProductUnit: String,
        halfProductHostUnit: String
    ) {
        val dish = getDishData(dishModelId)
        val dishNewTotalPrice = dish.totalPrice + totalPriceOfHalfProduct(
            pricePerUnit,
            weight,
            halfProductUnit,
            halfProductHostUnit
        )
        setDishData(dishModelId, dishNewTotalPrice, dish.margin, dish.tax)
    }

    private fun totalPriceOfHalfProduct(
        pricePerUnit: Double,
        weight: Double,
        halfProductUnit: String,
        halfProductHostUnit: String
    ): Double {
        return calculatePrice(pricePerUnit, weight, halfProductUnit, halfProductHostUnit)
    }

    private val dishMap: MutableMap<Long, DishPriceData> = mutableMapOf()

    fun setDishData(dishModelId: Long, totalPrice: Double, margin: Double, tax: Double) {
        val newData = DishPriceData(totalPrice, margin, tax)
        dishMap[dishModelId] = newData
    }

    private fun getDishData(dishModelId: Long): DishPriceData {
        return dishMap[dishModelId] ?: DishPriceData(0.0,0.0,0.0)
    }

}

data class DishPriceData(val totalPrice: Double, val margin: Double, val tax: Double)
