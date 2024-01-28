package com.erdees.foodcostcalc.ui.fragments.dishesFragment

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.erdees.foodcostcalc.data.AppRoomDataBase
import com.erdees.foodcostcalc.data.searchengine.SearchEngineRepository
import com.erdees.foodcostcalc.data.grandDish.GrandDishRepository
import com.erdees.foodcostcalc.data.halfProductWithProductsIncluded.HalfProductWithProductsIncludedRepository
import com.erdees.foodcostcalc.domain.model.dish.DishPriceData
import com.erdees.foodcostcalc.utils.UnitsUtils
import com.erdees.foodcostcalc.utils.Utils

class DishesFragmentViewModel(application: Application) : AndroidViewModel(application) {

  private val grandDishRepository: GrandDishRepository
  private val halfProductWithProductsIncludedRepository: HalfProductWithProductsIncludedRepository
  private val searchEngineRepository = SearchEngineRepository.getInstance()
  val idToQuantityMap = mutableMapOf<Long, Int>()
  val expandedList = mutableListOf<Long>()

  fun determineIfDishIsExpanded(dishModelId: Long): Boolean {
    return expandedList.contains(dishModelId)
  }

  init {
    val halfProductWithProductsIncludedDao =
      AppRoomDataBase.getDatabase(application).halfProductWithProductsIncludedDao()
    val grandDishDao = AppRoomDataBase.getDatabase(application).grandDishDao()
    grandDishRepository = GrandDishRepository.getInstance(grandDishDao)
    halfProductWithProductsIncludedRepository =
      HalfProductWithProductsIncludedRepository(halfProductWithProductsIncludedDao)
  }

  fun getGrandDishes() = grandDishRepository.getGrandDishes()

  fun getWhatToSearchFor() = searchEngineRepository.getWhatToSearchFor()

  fun getCertainHalfProductWithProductsIncluded(halfProductId: Long) =
    halfProductWithProductsIncludedRepository.getCertainHalfProductWithProductsIncluded(
      halfProductId
    )

  fun formattedPriceData(dishModelId: Long, amountOfServings: Int, context: Context): String {
    return Utils.formatPrice(getDishData(dishModelId).totalPrice * amountOfServings, context)
  }

  fun formattedTotalPriceData(dishModelId: Long, amountOfServings: Int, context: Context): String {
    val dishData = getDishData(dishModelId)
    return formattedTotalPriceData(
      dishData.totalPrice,
      dishData.margin,
      dishData.tax,
      amountOfServings,
      context
    )
  }

  private fun formattedTotalPriceData(
    totalPrice: Double,
    dishMargin: Double,
    dishTax: Double,
    amountOfServings: Int,
    context: Context
  ): String {
    return Utils.formatPrice(
      priceAfterMarginAndTax(
        totalPrice,
        dishMargin,
        dishTax,
        amountOfServings
      ), context
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
    return UnitsUtils.calculatePrice(pricePerUnit, weight, halfProductUnit, halfProductHostUnit)
  }

  private val dishMap: MutableMap<Long, DishPriceData> = mutableMapOf()

  fun setDishData(dishModelId: Long, totalPrice: Double, margin: Double, tax: Double) {
    val newData = DishPriceData(totalPrice, margin, tax)
    dishMap[dishModelId] = newData
  }

  private fun getDishData(dishModelId: Long): DishPriceData {
    return dishMap[dishModelId] ?: DishPriceData(0.0, 0.0, 0.0)
  }
}
