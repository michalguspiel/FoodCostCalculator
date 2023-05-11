package com.erdees.foodcostcalc.ui.fragments.dishesFragment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.erdees.foodcostcalc.data.AppRoomDataBase
import com.erdees.foodcostcalc.data.searchengine.SearchEngineRepository
import com.erdees.foodcostcalc.data.grandDish.GrandDishRepository

class DishesFragmentViewModel(application: Application) : AndroidViewModel(application) {

    private val grandDishRepository: GrandDishRepository
    private val searchEngineRepository = SearchEngineRepository.getInstance()

    init {
        val grandDishDao = AppRoomDataBase.getDatabase(application).grandDishDao()
        grandDishRepository = GrandDishRepository(grandDishDao)
    }

    fun getGrandDishes() = grandDishRepository.getGrandDishes()

    fun getWhatToSearchFor() = searchEngineRepository.getWhatToSearchFor()
}
