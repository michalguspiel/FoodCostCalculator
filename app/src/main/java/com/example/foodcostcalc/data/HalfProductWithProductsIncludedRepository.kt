package com.example.foodcostcalc.data

import androidx.lifecycle.LiveData
import com.example.foodcostcalc.model.DishWithHalfProductCrossRef
import com.example.foodcostcalc.model.HalfProductWithProductsIncludedCrossRef

class HalfProductWithProductsIncludedRepository(private val halfProductWithProductsIncludedDao : HalfProductWithProductsIncludedDao) {

    val readAllData = halfProductWithProductsIncludedDao.getHalfProductsWithProductsIncluded()

    suspend fun addHalfProductWithProductsIncludedCrossRef(halfProductWithProductsIncludedCrossRef:
                                                           HalfProductWithProductsIncludedCrossRef){
        halfProductWithProductsIncludedDao
            .addHalfProductWithProductsIncludedCrossRef(halfProductWithProductsIncludedCrossRef)
    }

    suspend fun addHalfProductToDish(dishWithHalfProductCrossRef: DishWithHalfProductCrossRef){
        halfProductWithProductsIncludedDao.addHalfProductToDish(dishWithHalfProductCrossRef)
    }


    companion object{
        @Volatile
        private var instance:HalfProductWithProductsIncludedRepository? = null

        fun getInstance(halfProductWithProductsIncludedDao: HalfProductWithProductsIncludedDao) =
            instance?: synchronized(this){
                instance?: HalfProductWithProductsIncludedRepository(halfProductWithProductsIncludedDao).also { instance = it }
            }
    }
}