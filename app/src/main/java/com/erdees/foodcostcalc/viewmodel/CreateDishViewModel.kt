package com.erdees.foodcostcalc.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.AppRoomDataBase
import com.erdees.foodcostcalc.data.dish.DishRepository
import com.erdees.foodcostcalc.model.Dish
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CreateDishViewModel(application: Application): AndroidViewModel(application) {

    private val dishRepository: DishRepository

    init {
        val dishDao = AppRoomDataBase.getDatabase(application).dishDao()
        dishRepository = DishRepository(dishDao)
    }

    fun addDish(dish: Dish) {
        viewModelScope.launch(Dispatchers.IO) {
            dishRepository.addDish(dish)
        }
    }
}