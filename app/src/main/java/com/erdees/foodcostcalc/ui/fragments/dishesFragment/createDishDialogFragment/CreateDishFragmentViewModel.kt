package com.erdees.foodcostcalc.ui.fragments.dishesFragment.createDishDialogFragment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.AppRoomDataBase
import com.erdees.foodcostcalc.data.dish.DishRepository
import com.erdees.foodcostcalc.ui.fragments.dishesFragment.models.DishModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CreateDishFragmentViewModel(application: Application) : AndroidViewModel(application) {

    private val dishRepository: DishRepository

    init {
        val dishDao = AppRoomDataBase.getDatabase(application).dishDao()
        dishRepository = DishRepository(dishDao)
    }

    fun addDish(dishModel: DishModel) {
        viewModelScope.launch(Dispatchers.IO) {
            dishRepository.addDish(dishModel)
        }
    }
}