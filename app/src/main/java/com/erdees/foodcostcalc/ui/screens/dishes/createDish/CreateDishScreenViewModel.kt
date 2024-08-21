package com.erdees.foodcostcalc.ui.screens.dishes.createDish

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.Preferences
import com.erdees.foodcostcalc.data.model.DishBase
import com.erdees.foodcostcalc.data.repository.DishRepository
import com.erdees.foodcostcalc.utils.Constants
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class CreateDishScreenViewModel : ViewModel(), KoinComponent {

  private val dishRepository: DishRepository by inject()
  private val firebaseAnalytics: FirebaseAnalytics by inject()

  private val sharedPreferences: Preferences by inject()


  var dishName = MutableStateFlow("")
  var margin = MutableStateFlow(sharedPreferences.defaultMargin)
  var tax = MutableStateFlow(sharedPreferences.defaultTax)

  private var _addedDish = MutableStateFlow<DishBase?>(null)
  val addedDish : StateFlow<DishBase?> = _addedDish

  private fun addDish(dish: DishBase) {
    viewModelScope.launch(Dispatchers.IO) {
      dishRepository.addDish(dish)
    }
  }

  fun resetAddedDish(){
    _addedDish.value = null
  }

  fun addDish() {
    if (margin.value.isEmpty()) margin.value = Constants.BASIC_MARGIN.toString()
    if (tax.value.isEmpty()) tax.value = Constants.BASIC_TAX.toString()
    val dish = DishBase(
      0,
      dishName.value,
      margin.value.toDoubleOrNull() ?: Constants.BASIC_MARGIN.toDouble(),
      tax.value.toDoubleOrNull() ?: Constants.BASIC_TAX.toDouble()
    )
    addDish(dish)
    sendEventDataToAnalytics(dish)
    _addedDish.value = dish
  }

  private fun sendEventDataToAnalytics(dish: DishBase) {
    val thisDishBundle = Bundle()
    thisDishBundle.putString(Constants.DISH_NAME, dish.name)
    firebaseAnalytics.logEvent(Constants.DISH_CREATED, thisDishBundle)
  }
}
