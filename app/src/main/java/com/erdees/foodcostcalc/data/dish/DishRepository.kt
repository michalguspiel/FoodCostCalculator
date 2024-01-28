package com.erdees.foodcostcalc.data.dish

import androidx.lifecycle.LiveData
import com.erdees.foodcostcalc.domain.model.dish.DishModel

class DishRepository(private val dishDao: DishDao) {

    val readAllData: LiveData<List<DishModel>> = dishDao.getDishes()

    suspend fun addDish(dishModel: DishModel) {
        dishDao.addDish(dishModel)
    }

  suspend fun deleteDish(dishModel: DishModel) {
        dishDao.deleteDish(dishModel)
    }

    suspend fun editDish(dishModel: DishModel) {
        dishDao.editDish(dishModel)
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
