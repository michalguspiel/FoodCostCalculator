package com.example.foodcostcalc.data

import androidx.lifecycle.LiveData
import com.example.foodcostcalc.model.HalfProduct
import com.example.foodcostcalc.model.HalfProductWithProductsIncluded
import com.example.foodcostcalc.model.ProductIncludedInHalfProduct

class ProductIncludedInHalfProductRepository(private val productIncludedInHalfProductDao: ProductIncludedInHalfProductDao) {
    val readAllData : LiveData<List<ProductIncludedInHalfProduct>> = productIncludedInHalfProductDao.getAllProductIncludedInHalfProduct()

    suspend fun addProductIncludedInHalfProduct(productIncludedInHalfProduct: ProductIncludedInHalfProduct)
    = productIncludedInHalfProductDao.addProductIncludedInHalfProduct(productIncludedInHalfProduct)

    suspend fun editProductIncludedInHalfProduct(productIncludedInHalfProduct: ProductIncludedInHalfProduct)
    = productIncludedInHalfProductDao.editProductIncludedInHalfProduct(productIncludedInHalfProduct)

    suspend fun deleteProductIncludedInHalfProduct(productIncludedInHalfProduct: ProductIncludedInHalfProduct)
    = productIncludedInHalfProductDao.deleteProductIncludedInHalfProduct(productIncludedInHalfProduct)

    companion object{
        @Volatile
        private var instance:ProductIncludedInHalfProductRepository? = null

        fun getInstance(productIncludedInHalfProductDao: ProductIncludedInHalfProductDao) =
            instance?: synchronized(this){
                instance?: ProductIncludedInHalfProductRepository(productIncludedInHalfProductDao).also { instance = it }
            }
    }
}