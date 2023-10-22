package com.erdees.foodcostcalc.data.halfProductIncludedInDish

import com.erdees.foodcostcalc.domain.model.halfProduct.HalfProductIncludedInDishModel

class HalfProductIncludedInDishRepository(private val halfProductIncludedInDishDao: HalfProductIncludedInDishDao ) {

    fun getHalfProductsIncludedInDishFromDishByHalfProduct(productId: Long) =
        halfProductIncludedInDishDao.getHalfProductsIncludedInDishFromDishByHalfProduct(productId)

    suspend fun addHalfProductIncludedInDish(halfProductIncludedInDishModel: HalfProductIncludedInDishModel) =
        halfProductIncludedInDishDao.addHalfProductIncludedInDish(halfProductIncludedInDishModel)

    suspend fun editHalfProductIncludedInDish(halfProductIncludedInDishModel: HalfProductIncludedInDishModel) =
        halfProductIncludedInDishDao.editHalfProductIncludedInDish(halfProductIncludedInDishModel)

    suspend fun deleteHalfProductIncludedInDish(halfProductIncludedInDishModel: HalfProductIncludedInDishModel) =
        halfProductIncludedInDishDao.deleteHalfProductIncludedInDish(halfProductIncludedInDishModel)

    fun deleteAllHalfProductsIncludedInDish(dishId: Long) {
        halfProductIncludedInDishDao.deleteAllHalfProductsIncludedInDish(dishId)
    }

}
