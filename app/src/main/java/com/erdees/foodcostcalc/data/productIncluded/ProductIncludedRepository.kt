package com.erdees.foodcostcalc.data.productIncluded

import com.erdees.foodcostcalc.ui.fragments.productsFragment.models.ProductIncluded

class ProductIncludedRepository(val productIncludedDao: ProductIncludedDao) {

    suspend fun addProductToDish(product: ProductIncluded) = productIncludedDao.addProductToDish(product)


    suspend fun editProductsIncluded(productIncluded: ProductIncluded){
        productIncludedDao.editProductsIncluded(productIncluded)
    }

    suspend fun deleteProductIncluded(productIncluded: ProductIncluded){
        productIncludedDao.deleteProductIncluded(productIncluded)
    }



    fun getIngredientsFromDish(dishId: Long) = productIncludedDao.getIngredientsFromDish(dishId)

    fun getCertainProductIncluded(id: Long) = productIncludedDao.getCertainProductsIncluded(id)

    fun getProductIncludedByDishID(id: Long) = productIncludedDao.getProductIncludedFromDishId(id)

}