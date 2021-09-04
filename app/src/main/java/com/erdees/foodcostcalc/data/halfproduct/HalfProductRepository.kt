package com.erdees.foodcostcalc.data.halfproduct

import androidx.lifecycle.LiveData
import com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.models.HalfProductModel

class HalfProductRepository(private val halfProductDao: HalfProductDao) {
    val readAllData: LiveData<List<HalfProductModel>> = halfProductDao.getHalfProducts()

    suspend fun addHalfProduct(halfProductModel: HalfProductModel) =
        halfProductDao.addHalfProduct(halfProductModel)

    suspend fun editHalfProduct(halfProductModel: HalfProductModel) =
        halfProductDao.editHalfProduct(halfProductModel)

    suspend fun deleteHalfProduct(halfProductModel: HalfProductModel) =
        halfProductDao.deleteHalfProduct(halfProductModel)

    companion object{
        @Volatile
        private var instance: HalfProductRepository? = null

        fun getInstance(halfProductDao: HalfProductDao) =
            instance ?: synchronized(this){
                instance ?: HalfProductRepository(halfProductDao).also { instance = it }
            }
    }
}