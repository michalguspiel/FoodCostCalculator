package com.erdees.foodcostcalc.data.halfproduct

import androidx.lifecycle.LiveData
import com.erdees.foodcostcalc.domain.model.halfProduct.HalfProductModel

class HalfProductRepository(private val halfProductDao: HalfProductDao) {
    val readAllData: LiveData<List<HalfProductModel>> = halfProductDao.getHalfProducts()

    suspend fun addHalfProduct(halfProductModel: HalfProductModel) =
        halfProductDao.addHalfProduct(halfProductModel)

    suspend fun editHalfProduct(halfProductModel: HalfProductModel) =
        halfProductDao.editHalfProduct(halfProductModel)

    suspend fun deleteHalfProduct(halfProductModel: HalfProductModel) =
        halfProductDao.deleteHalfProduct(halfProductModel)

    fun deleteHalfProduct(id: Long){
      halfProductDao.deleteHalfProduct(id)
    }

    companion object{
        @Volatile
        private var instance: HalfProductRepository? = null

        fun getInstance(halfProductDao: HalfProductDao) =
            instance ?: synchronized(this){
                instance ?: HalfProductRepository(halfProductDao).also { instance = it }
            }
    }
}
