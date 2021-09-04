package com.erdees.foodcostcalc.data.halfProductIncludedInDish

import com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.models.HalfProductIncludedInDishModel

class HalfProductIncludedInDishRepository(private val halfProductIncludedInDishDao: HalfProductIncludedInDishDao ) {

    fun getHalfProductsIncludedInDishFromDish(dishId: Long) =
        halfProductIncludedInDishDao.getHalfProductsIncludedInDishFromDish(dishId)

    fun getHalfProductsIncludedInDishFromDishByHalfProduct(productId: Long) =
        halfProductIncludedInDishDao.getHalfProductsIncludedInDishFromDishByHalfProduct(productId)

    suspend fun addHalfProductIncludedInDish(halfProductIncludedInDishModel: HalfProductIncludedInDishModel) =
        halfProductIncludedInDishDao.addHalfProductIncludedInDish(halfProductIncludedInDishModel)

    suspend fun editHalfProductIncludedInDish(halfProductIncludedInDishModel: HalfProductIncludedInDishModel) =
        halfProductIncludedInDishDao.editHalfProductIncludedInDish(halfProductIncludedInDishModel)

    suspend fun deleteHalfProductIncludedInDish(halfProductIncludedInDishModel: HalfProductIncludedInDishModel) =
        halfProductIncludedInDishDao.deleteHalfProductIncludedInDish(halfProductIncludedInDishModel)


    companion object {
        @Volatile
        private var instance: HalfProductIncludedInDishRepository? = null

        fun getInstance(halfProductIncludedInDishDao: HalfProductIncludedInDishDao) =
            instance ?: synchronized(this) {
                instance ?: HalfProductIncludedInDishRepository(halfProductIncludedInDishDao).also {
                    instance = it
                }
            }
    }

}