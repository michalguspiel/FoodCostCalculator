package com.erdees.foodcostcalc.ui.fragments.dishesFragment.createDishDialogFragment

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.Preferences
import com.erdees.foodcostcalc.data.model.Dish
import com.erdees.foodcostcalc.data.repository.DishRepository
import com.erdees.foodcostcalc.utils.Constants
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class CreateDishFragmentViewModel : ViewModel(), KoinComponent  {

    private val dishRepository: DishRepository by inject()
    private val  firebaseAnalytics: FirebaseAnalytics by inject()

    private val sharedPreferences: Preferences by inject()

    private var margin: String? = sharedPreferences.defaultMargin
    private var tax: String? = sharedPreferences.defaultTax

    private fun addDish(dish: Dish) {
        viewModelScope.launch(Dispatchers.IO) {
            dishRepository.addDish(dish)
        }
    }

    fun addDish(dishName: String): Dish {
        if (margin.isNullOrEmpty()) margin = Constants.BASIC_MARGIN.toString()
        if (tax.isNullOrEmpty()) tax = Constants.BASIC_TAX.toString()
        val dish = Dish(0, dishName, margin!!.toDouble(), tax!!.toDouble())
        addDish(dish)
        sendEventDataToAnalytics(dish)
        return dish
    }

    private fun sendEventDataToAnalytics(dish: Dish) {
        val thisDishBundle = Bundle()
        thisDishBundle.putString(Constants.DISH_NAME, dish.name)
        firebaseAnalytics.logEvent(Constants.DISH_CREATED, thisDishBundle)
    }
}
