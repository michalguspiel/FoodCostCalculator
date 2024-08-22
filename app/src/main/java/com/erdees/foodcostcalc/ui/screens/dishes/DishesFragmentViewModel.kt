package com.erdees.foodcostcalc.ui.screens.dishes

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.repository.DishRepository
import com.erdees.foodcostcalc.data.searchengine.SearchEngineRepository
import com.erdees.foodcostcalc.domain.mapper.Mapper.toDishDomain
import com.erdees.foodcostcalc.domain.model.dish.DishDomain
import com.erdees.foodcostcalc.utils.UnitsUtils
import com.erdees.foodcostcalc.utils.Utils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DishesFragmentViewModel : ViewModel(), KoinComponent {

  private val dishRepository: DishRepository by inject()
  private val searchEngineRepository = SearchEngineRepository.getInstance()

  private val searchedKey = searchEngineRepository.getWhatToSearchFor().asFlow().stateIn(
    viewModelScope,
    SharingStarted.Lazily, ""
  )

  private val _dishes = dishRepository.dishes.map { it.map { it.toDishDomain() } }.stateIn(
    viewModelScope,
    SharingStarted.Lazily,
    emptyList()
  )

  val dishes: StateFlow<List<DishDomain>> = combine(searchedKey, _dishes) { key, dishes ->
    dishes.filter {
      it.name.lowercase().contains(key.lowercase())
    }
  }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())


  val idToQuantityMap = mutableMapOf<Long, Int>()
  val expandedList = mutableListOf<Long>()

  fun determineIfDishIsExpanded(dishModelId: Long): Boolean {
    return expandedList.contains(dishModelId)
  }

  fun formattedPriceData(dishDomain: DishDomain, amountOfServings: Int, context: Context): String {
    return Utils.formatPrice(dishDomain.foodCost * amountOfServings, context)
  }

  fun formattedTotalPriceData(dishDomain: DishDomain, amountOfServings: Int, context: Context): String {
    return formattedTotalPriceData(
      dishDomain.foodCost,
      dishDomain.marginPercent,
      dishDomain.taxPercent,
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

  private fun totalPriceOfHalfProduct(
    pricePerUnit: Double,
    weight: Double,
    halfProductUnit: String,
    halfProductHostUnit: String
  ): Double {
    return UnitsUtils.calculatePrice(pricePerUnit, weight, halfProductUnit, halfProductHostUnit)
  }
}
