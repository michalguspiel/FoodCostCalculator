package com.erdees.foodcostcalc.data.halfProductIncludedInDish

import com.erdees.foodcostcalc.data.halfproduct.HalfProductDao
import com.erdees.foodcostcalc.data.halfproduct.HalfProductRepository
import com.erdees.foodcostcalc.model.HalfProductIncludedInDish

class HalfProductIncludedInDishRepository(private val halfProductIncludedInDishDao: HalfProductIncludedInDishDao ) {

    fun getHalfProductsIncludedInDishFromDish(dishId: Long) = halfProductIncludedInDishDao.getHalfProductsIncludedInDishFromDish(dishId)

    fun getHalfProductsIncludedInDishFromDishByHalfProduct(productId: Long) = halfProductIncludedInDishDao.getHalfProductsIncludedInDishFromDishByHalfProduct(productId)

    suspend fun addHalfProductIncludedInDish(halfProductIncludedInDish: HalfProductIncludedInDish)
            = halfProductIncludedInDishDao.addHalfProductIncludedInDish(halfProductIncludedInDish)

    suspend fun editHalfProductIncludedInDish(halfProductIncludedInDish: HalfProductIncludedInDish)
            = halfProductIncludedInDishDao.editHalfProductIncludedInDish(halfProductIncludedInDish)

    suspend fun deleteHalfProductIncludedInDish(halfProductIncludedInDish: HalfProductIncludedInDish)
            = halfProductIncludedInDishDao.deleteHalfProductIncludedInDish(halfProductIncludedInDish)


    companion object{
        @Volatile
        private var instance: HalfProductIncludedInDishRepository? = null

        fun getInstance(halfProductIncludedInDishDao: HalfProductIncludedInDishDao) =
            instance ?: synchronized(this){
                instance ?: HalfProductIncludedInDishRepository(halfProductIncludedInDishDao).also { instance = it }
            }
    }

}