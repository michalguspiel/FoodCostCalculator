package com.erdees.foodcostcalc.data.productIncluded

import com.erdees.foodcostcalc.domain.model.product.ProductIncluded

class ProductIncludedRepository(val productIncludedDao: ProductIncludedDao) {

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

    fun getProductIncludedByDishID(id: Long) = productIncludedDao.getProductIncludedFromDishId(id)

}
