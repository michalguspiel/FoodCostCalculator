package com.erdees.foodcostcalc.ui.fragments.dishesFragment.editDishDialogFragment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.AppRoomDataBase
import com.erdees.foodcostcalc.data.dish.DishRepository
import com.erdees.foodcostcalc.entities.Dish
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditDishFragmentViewModel(application: Application) : AndroidViewModel(application) {

  private val dishDao = AppRoomDataBase.getDatabase(application).dishDao()

  private val dishRepository: DishRepository = DishRepository.getInstance(dishDao)

  fun saveDish(dishId: Long, dishName: String, dishMargin: Double, dishTax: Double) {
    val dish = Dish(dishId, dishName, dishMargin, dishTax)
    viewModelScope.launch(Dispatchers.IO) {
      dishRepository.editDish(dish)
    }
  }

  private fun deleteDish(dish: Dish) {
    viewModelScope.launch(Dispatchers.IO) {
      dishRepository.deleteDish(dish)
    }
  }

}
