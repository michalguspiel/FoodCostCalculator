package com.erdees.foodcostcalc.data.productIncludedInHalfProduct

import androidx.lifecycle.LiveData
import com.erdees.foodcostcalc.domain.model.halfProduct.ProductIncludedInHalfProductModel

class ProductIncludedInHalfProductRepository(private val productIncludedInHalfProductDao: ProductIncludedInHalfProductDao) {
    val readAllData: LiveData<List<ProductIncludedInHalfProductModel>> =
        productIncludedInHalfProductDao.getAllProductIncludedInHalfProduct()

    val readAllDataNotAsc: LiveData<List<ProductIncludedInHalfProductModel>> =
        productIncludedInHalfProductDao.getAllProductIncludedInHalfProductNotAsc()

    fun getCertainProductsIncluded(productId: Long) =
        productIncludedInHalfProductDao.getCertainProductsIncluded(productId)

    suspend fun addProductIncludedInHalfProduct(productIncludedInHalfProductModel: ProductIncludedInHalfProductModel) =
        productIncludedInHalfProductDao.addProductIncludedInHalfProduct(
            productIncludedInHalfProductModel
        )

    suspend fun editProductIncludedInHalfProduct(productIncludedInHalfProductModel: ProductIncludedInHalfProductModel) =
        productIncludedInHalfProductDao.editProductIncludedInHalfProduct(
            productIncludedInHalfProductModel
        )

    suspend fun deleteProductIncludedInHalfProduct(productIncludedInHalfProductModel: ProductIncludedInHalfProductModel) =
        productIncludedInHalfProductDao.deleteProductIncludedInHalfProduct(
            productIncludedInHalfProductModel
        )

    fun getProductsIncludedFromHalfProduct(halfProductId: Long) =
        productIncludedInHalfProductDao.getProductsFromHalfProduct(halfProductId)

    fun deleteProductsIncludedInHalfProduct(id: Long){
      productIncludedInHalfProductDao.deleteAllProductsIncludedInHalfProduct(id)
    }

    companion object {
        @Volatile
        private var instance: ProductIncludedInHalfProductRepository? = null

        fun getInstance(productIncludedInHalfProductDao: ProductIncludedInHalfProductDao) =
            instance ?: synchronized(this){
                instance ?: ProductIncludedInHalfProductRepository(productIncludedInHalfProductDao).also { instance = it }
            }
    }
}
