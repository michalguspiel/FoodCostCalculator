package com.erdees.foodcostcalc.data.dish

import androidx.lifecycle.LiveData
import com.erdees.foodcostcalc.model.Dish
import com.erdees.foodcostcalc.model.ProductIncluded

class DishRepository(private val dishDao: DishDao) {

    val readAllData: LiveData<List<Dish>> = dishDao.getDishes()

    suspend fun addDish(dish: Dish) {
        dishDao.addDish(dish)
    }

    fun getDishes() = dishDao.getDishes()

    suspend fun deleteDish(dish: Dish){
        dishDao.deleteDish(dish)
    }

    suspend fun editDish(dish: Dish){
        dishDao.editDish(dish)
    }


    companion object {
        @Volatile
        private var instance: DishRepository? = null

        fun getInstance(dishDao: DishDao) =
            instance ?: synchronized(this) {
                instance
                    ?: DishRepository(dishDao).also { instance = it }
            }
    }
}
