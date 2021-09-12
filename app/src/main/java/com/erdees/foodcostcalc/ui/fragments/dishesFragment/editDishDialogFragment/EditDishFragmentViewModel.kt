package com.erdees.foodcostcalc.ui.fragments.dishesFragment.editDishDialogFragment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.erdees.foodcostcalc.data.AppRoomDataBase
import com.erdees.foodcostcalc.data.basic.BasicDataBase
import com.erdees.foodcostcalc.data.basic.BasicRepository
import com.erdees.foodcostcalc.data.dish.DishRepository
import com.erdees.foodcostcalc.data.grandDish.GrandDishRepository

/**TODO REFACTORING INTO VIEW BINDING + MVVM PATTERN IMPROVEMENT */


class EditDishFragmentViewModel(application: Application) : AndroidViewModel(application) {

    val dishRepository: DishRepository
    val grandDishRepository: GrandDishRepository
    val basicRepository: BasicRepository

    init {
        val dishDao = AppRoomDataBase.getDatabase(application).dishDao()
        val grandDishDao = AppRoomDataBase.getDatabase(application).grandDishDao()
        val basicDao = BasicDataBase.getInstance().basicDao

        dishRepository = DishRepository(dishDao)
        grandDishRepository = GrandDishRepository(grandDishDao)
        basicRepository = BasicRepository(basicDao)
    }

    fun getDishes() = dishRepository.getDishes()


    fun getGrandDishById(dishId: Long) = grandDishRepository.getGrandDishById(dishId)

    /**Basic repository methods*/
    fun setFlag(boolean: Boolean) {
        basicRepository.setFlag(boolean)
    }

    fun setPosition(pos: Int) {
        basicRepository.setPosition(pos)
    }

    fun getFlag() = basicRepository.getFlag()

    fun getPosition() = basicRepository.getPosition()


}