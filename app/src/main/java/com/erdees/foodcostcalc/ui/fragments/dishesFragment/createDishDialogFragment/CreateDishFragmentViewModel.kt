package com.erdees.foodcostcalc.ui.fragments.dishesFragment.createDishDialogFragment

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.AppRoomDataBase
import com.erdees.foodcostcalc.data.dish.DishRepository
import com.erdees.foodcostcalc.domain.model.dish.DishModel
import com.erdees.foodcostcalc.ui.fragments.settingsFragment.SharedPreferences
import com.erdees.foodcostcalc.utils.Constants
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CreateDishFragmentViewModel(application: Application) : AndroidViewModel(application) {

    private val dishRepository: DishRepository

    lateinit var firebaseAnalytics: FirebaseAnalytics

    val sharedPreferences = SharedPreferences(application)

    init {
        val dishDao = AppRoomDataBase.getDatabase(application).dishDao()
        dishRepository = DishRepository(dishDao)
    }

    private var margin: String? = ""
    private var tax: String? = ""

    fun updateMarginAndTax() {
        margin = sharedPreferences.getValueString(Constants.MARGIN)
        tax = sharedPreferences.getValueString(Constants.TAX)
    }

    private fun addDish(dishModel: DishModel) {
        viewModelScope.launch(Dispatchers.IO) {
            dishRepository.addDish(dishModel)
        }
    }

    fun addDish(dishName: String): DishModel {
        if (margin.isNullOrEmpty()) margin = Constants.BASIC_MARGIN.toString()
        if (tax.isNullOrEmpty()) tax = Constants.BASIC_TAX.toString()
        val dish = DishModel(0, dishName, margin!!.toDouble(), tax!!.toDouble())
        addDish(dish)
        sendEventDataToAnalytics(dish)
        return dish
    }

    private fun sendEventDataToAnalytics(dishModel: DishModel) {
        val thisDishBundle = Bundle()
        thisDishBundle.putString(Constants.DISH_NAME, dishModel.name)
        firebaseAnalytics.logEvent(Constants.DISH_CREATED, thisDishBundle)
    }
}
