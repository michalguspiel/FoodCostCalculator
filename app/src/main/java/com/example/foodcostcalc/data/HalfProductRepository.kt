package com.example.foodcostcalc.data

import androidx.lifecycle.LiveData
import com.example.foodcostcalc.model.HalfProduct

class HalfProductRepository(private val halfProductDao: HalfProductDao) {
    val readAllData : LiveData<List<HalfProduct>> = halfProductDao.getHalfProducts()

    suspend fun addHalfProduct(halfProduct: HalfProduct) = halfProductDao.addHalfProduct(halfProduct)

    suspend fun editHalfProduct(halfProduct: HalfProduct) = halfProductDao.editHalfProduct(halfProduct)

    suspend fun deleteHalfProduct(halfProduct: HalfProduct) = halfProductDao.deleteHalfProduct(halfProduct)

    companion object{
        @Volatile
        private var instance:HalfProductRepository? = null

        fun getInstance(halfProductDao: HalfProductDao) =
            instance?: synchronized(this){
                instance?: HalfProductRepository(halfProductDao).also { instance = it }
            }
    }
}