package com.erdees.foodcostcalc.data.productIncluded

import com.erdees.foodcostcalc.domain.model.product.ProductIncluded

class ProductIncludedRepository(private val productIncludedDao: ProductIncludedDao) {

    suspend fun addProductToDish(product: ProductIncluded) =
        productIncludedDao.addProductToDish(product)

    suspend fun editProductsIncluded(productIncluded: ProductIncluded) {
        productIncludedDao.editProductsIncluded(productIncluded)
    }

    suspend fun deleteProductIncluded(productIncluded: ProductIncluded) {
        productIncludedDao.deleteProductIncluded(productIncluded)
    }

    fun deleteAllProductsIncludedInDish(dishId: Long) {
        productIncludedDao.deleteAllProductsIncludedInDish(dishId)
    }

    fun getCertainProductIncluded(id: Long) = productIncludedDao.getCertainProductsIncluded(id)

  companion object {
    @Volatile
    private var instance: ProductIncludedRepository? = null

    fun getInstance(productIncludedDao: ProductIncludedDao) =
      instance ?: synchronized(this) {
        instance
          ?: ProductIncludedRepository(productIncludedDao).also { instance = it }
      }
  }
}
