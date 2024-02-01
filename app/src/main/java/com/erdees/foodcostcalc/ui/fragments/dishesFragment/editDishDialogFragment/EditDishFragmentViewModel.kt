package com.erdees.foodcostcalc.ui.fragments.dishesFragment.editDishDialogFragment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.AppRoomDataBase
import com.erdees.foodcostcalc.data.dish.DishRepository
import com.erdees.foodcostcalc.data.grandDish.GrandDishRepository
import com.erdees.foodcostcalc.data.halfProductIncludedInDish.HalfProductIncludedInDishRepository
import com.erdees.foodcostcalc.data.productIncluded.ProductIncludedRepository
import com.erdees.foodcostcalc.entities.Dish
import com.erdees.foodcostcalc.domain.model.dish.GrandDish
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditDishFragmentViewModel(application: Application) : AndroidViewModel(application) {

    private val dishRepository: DishRepository
    private val grandDishRepository: GrandDishRepository
    private val productIncludedRepository: ProductIncludedRepository
    private val halfProductIncludedInDishRepository: HalfProductIncludedInDishRepository

    init {
        val dishDao = AppRoomDataBase.getDatabase(application).dishDao()
        val grandDishDao = AppRoomDataBase.getDatabase(application).grandDishDao()
        val productIncludedDao = AppRoomDataBase.getDatabase(application).productIncludedDao()
        val halfProductIncludedInDishDao =
            AppRoomDataBase.getDatabase(application).halfProductIncludedInDishDao()

        dishRepository = DishRepository.getInstance(dishDao)
        grandDishRepository = GrandDishRepository.getInstance(grandDishDao)
        productIncludedRepository = ProductIncludedRepository((productIncludedDao))
        halfProductIncludedInDishRepository =
            HalfProductIncludedInDishRepository.getInstance(halfProductIncludedInDishDao)
    }

    fun saveDish(dishId: Long, dishName: String, dishMargin: Double, dishTax: Double) {
        val dish = Dish(dishId, dishName, dishMargin, dishTax)
        viewModelScope.launch(Dispatchers.IO) {
            dishRepository.editDish(dish)
        }
    }

    fun getGrandDishById(dishId: Long) = grandDishRepository.getGrandDishById(dishId)

    fun deleteGrandDish(grandDish: GrandDish) {
        deleteDish(grandDish.dish)
        deleteAllProductsIncludedInThisDish(grandDish.dish.dishId)
        deleteAllHalfProductsIncludedInThisDish(grandDish.dish.dishId)
    }

    private fun deleteDish(dish: Dish) {
        viewModelScope.launch(Dispatchers.IO) {
            dishRepository.deleteDish(dish)
        }
    }

    private fun deleteAllProductsIncludedInThisDish(dishId: Long) {
        productIncludedRepository.deleteAllProductsIncludedInDish(dishId)
    }

    private fun deleteAllHalfProductsIncludedInThisDish(dishId: Long) {
        halfProductIncludedInDishRepository.deleteAllHalfProductsIncludedInDish(dishId)
    }

}
