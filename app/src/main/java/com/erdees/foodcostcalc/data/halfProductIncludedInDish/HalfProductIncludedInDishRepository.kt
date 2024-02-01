package com.erdees.foodcostcalc.data.halfProductIncludedInDish

import com.erdees.foodcostcalc.entities.HalfProductIncludedInDish

class HalfProductIncludedInDishRepository(private val halfProductIncludedInDishDao: HalfProductIncludedInDishDao) {

  fun getHalfProductsIncludedInDishFromDishByHalfProduct(productId: Long) =
    halfProductIncludedInDishDao.getHalfProductsIncludedInDishFromDishByHalfProduct(productId)

  fun addHalfProductIncludedInDish(halfProductIncludedInDish: HalfProductIncludedInDish) =
    halfProductIncludedInDishDao.addHalfProductIncludedInDish(halfProductIncludedInDish)

  suspend fun editHalfProductIncludedInDish(halfProductIncludedInDish: HalfProductIncludedInDish) =
    halfProductIncludedInDishDao.editHalfProductIncludedInDish(halfProductIncludedInDish)

  suspend fun deleteHalfProductIncludedInDish(halfProductIncludedInDish: HalfProductIncludedInDish) =
    halfProductIncludedInDishDao.deleteHalfProductIncludedInDish(halfProductIncludedInDish)

  fun deleteAllHalfProductsIncludedInDish(dishId: Long) {
    halfProductIncludedInDishDao.deleteAllHalfProductsIncludedInDish(dishId)
  }

  companion object {
    @Volatile
    private var instance: HalfProductIncludedInDishRepository? = null
    fun getInstance(halfProductIncludedInDishDao: HalfProductIncludedInDishDao): HalfProductIncludedInDishRepository =
      instance ?: synchronized(this) {
        instance
          ?: HalfProductIncludedInDishRepository(halfProductIncludedInDishDao).also {
            instance = it
          }
      }
  }
}
