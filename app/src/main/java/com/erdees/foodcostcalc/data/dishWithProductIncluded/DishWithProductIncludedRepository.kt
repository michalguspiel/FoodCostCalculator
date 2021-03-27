package com.erdees.foodcostcalc.data.dishWithProductIncluded

import com.erdees.foodcostcalc.data.dish.DishDao
import com.erdees.foodcostcalc.data.dish.DishRepository

class DishWithProductIncludedRepository(private val dao : DishWithProductIncludedDao) {

    fun getDishesWithProductsIncluded() = dao.getDishesWithProductsIncluded()

    companion object {
        @Volatile
        private var instance: DishWithProductIncludedRepository? = null

        fun getInstance(dishWithProductIncludedDao: DishWithProductIncludedDao) =
            instance ?: synchronized(this) {
                instance
                    ?: DishWithProductIncludedRepository(dishWithProductIncludedDao).also { instance = it }
            }
    }
}