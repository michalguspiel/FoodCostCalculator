package com.example.foodcostcalc.data

import androidx.lifecycle.LiveData
import com.example.foodcostcalc.model.Dish
import com.example.foodcostcalc.model.ProductIncluded

class DishRepository(private val dishDao: DishDao) {

    val readAllData: LiveData<List<Dish>> = dishDao.getDishes()

    suspend fun addDish(dish: Dish) {
        dishDao.addDish(dish)
    }

    fun getDishes() = dishDao.getDishes()

    fun getDishesByName(name: String) = dishDao.getDishesByName(name)

    suspend fun deleteDish(dish: Dish){
        dishDao.deleteDish(dish)
    }

    suspend fun editDish(dish: Dish){
        dishDao.editDish(dish)
    }

    suspend fun editProductsIncluded(productIncluded: ProductIncluded){
        dishDao.editProductsIncluded(productIncluded)
    }

    suspend fun deleteProductIncluded(productIncluded: ProductIncluded){
        dishDao.deleteProductIncluded(productIncluded)
    }

    fun getDishesWithProductsIncluded() = dishDao.getDishesWithProductsIncluded()


    fun getIngredientsFromDish(dishId: Long) = dishDao.getIngredientsFromDish(dishId)

    fun getCertainProductIncluded(id: Long) = dishDao.getCertainProductsIncluded(id)

    companion object {
        // Singleton instantiation you already know and love
        @Volatile
        private var instance: DishRepository? = null

        fun getInstance(dishDao: DishDao) =
            instance ?: synchronized(this) {
                instance
                    ?: DishRepository(dishDao).also { instance = it }
            }
    }
}
