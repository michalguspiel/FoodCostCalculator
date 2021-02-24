package com.example.foodcostcalc.data

import androidx.lifecycle.LiveData

class HalfProductWithProductsIncludedRepository(private val halfProductWithProductsIncludedDao : HalfProductWithProductsIncludedDao) {

    val readAllData = halfProductWithProductsIncludedDao.getHalfProductsWithProductsIncluded()

    companion object{
        @Volatile
        private var instance:HalfProductWithProductsIncludedRepository? = null

        fun getInstance(halfProductWithProductsIncludedDao: HalfProductWithProductsIncludedDao) =
            instance?: synchronized(this){
                instance?: HalfProductWithProductsIncludedRepository(halfProductWithProductsIncludedDao).also { instance = it }
            }
    }
}