package com.erdees.foodcostcalc.ui.screens.dishes.editDishDialogFragment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.model.DishBase
import com.erdees.foodcostcalc.data.repository.DishRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class EditDishFragmentViewModel : ViewModel(), KoinComponent {

  private val dishRepository: DishRepository by inject()

  fun saveDish(dishId: Long, dishName: String, dishMargin: Double, dishTax: Double) {
    val dish = DishBase(dishId, dishName, dishMargin, dishTax)
    viewModelScope.launch(Dispatchers.IO) {
      dishRepository.editDish(dish)
    }
  }

  private fun deleteDish(dish: DishBase) {
    viewModelScope.launch(Dispatchers.IO) {
      dishRepository.deleteDish(dish)
    }
  }
}
