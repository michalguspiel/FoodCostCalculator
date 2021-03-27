package com.erdees.foodcostcalc.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.erdees.foodcostcalc.data.AppRoomDataBase
import com.erdees.foodcostcalc.data.basic.BasicDataBase
import com.erdees.foodcostcalc.data.basic.BasicRepository
import com.erdees.foodcostcalc.data.dish.DishRepository
import com.erdees.foodcostcalc.data.grandDish.GrandDishRepository
import com.erdees.foodcostcalc.model.Dish

class EditDishViewModel(application: Application) : AndroidViewModel(application) {

    val dishRepository : DishRepository
    val grandDishRepository : GrandDishRepository
    val basicRepository : BasicRepository

    init {
        val dishDao = AppRoomDataBase.getDatabase(application).dishDao()
        val grandDishDao = AppRoomDataBase.getDatabase(application).grandDishDao()
        val basicDao = BasicDataBase.getInstance().basicDao

        dishRepository = DishRepository(dishDao)
        grandDishRepository = GrandDishRepository(grandDishDao)
        basicRepository = BasicRepository(basicDao)
    }

    fun getDishes() = dishRepository.getDishes()

    fun getGrandDishes() = grandDishRepository.getGrandDishes()

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