package com.example.foodcostcalc.data.halfproduct

import androidx.lifecycle.LiveData
import com.example.foodcostcalc.model.HalfProduct
import com.example.foodcostcalc.model.HalfProductIncludedInDish

class HalfProductRepository(private val halfProductDao: HalfProductDao) {
    val readAllData : LiveData<List<HalfProduct>> = halfProductDao.getHalfProducts()

    suspend fun addHalfProductIncludedInDish(halfProductIncludedInDish: HalfProductIncludedInDish)
    = halfProductDao.addHalfProductIncludedInDish(halfProductIncludedInDish)

    fun getHalfProductsFromDish(dishId: Long) = halfProductDao.getHalfProductsFromDish(dishId)

    fun getHalfProductsFromDishByHalfProduct(productId: Long) = halfProductDao.getHalfProductsFromDishByHalfProduct(productId)

    suspend fun editHalfProductIncludedInDish(halfProductIncludedInDish: HalfProductIncludedInDish)
    = halfProductDao.editHalfProductIncludedInDish(halfProductIncludedInDish)

    suspend fun deleteHalfProductIncludedInDish(halfProductIncludedInDish: HalfProductIncludedInDish)
    =halfProductDao.deleteHalfProductIncludedInDish(halfProductIncludedInDish)


    suspend fun addHalfProduct(halfProduct: HalfProduct) = halfProductDao.addHalfProduct(halfProduct)

    suspend fun editHalfProduct(halfProduct: HalfProduct) = halfProductDao.editHalfProduct(halfProduct)

    suspend fun deleteHalfProduct(halfProduct: HalfProduct) = halfProductDao.deleteHalfProduct(halfProduct)

    companion object{
        @Volatile
        private var instance: HalfProductRepository? = null

        fun getInstance(halfProductDao: HalfProductDao) =
            instance ?: synchronized(this){
                instance ?: HalfProductRepository(halfProductDao).also { instance = it }
            }
    }
}