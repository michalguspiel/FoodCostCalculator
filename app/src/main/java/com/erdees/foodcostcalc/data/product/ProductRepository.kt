package com.erdees.foodcostcalc.data.product

import androidx.lifecycle.LiveData
import com.erdees.foodcostcalc.ui.fragments.productsFragment.models.ProductModel

// Dao must be passed in - it is a dependency
// You could also instantiate the DAO right inside the class without all the fuss, right?
// No. This would break testability - you need to be able to pass a mock version of a DAO
// to the repository (e.g. one that upon calling getProducts() returns a dummy list of products for testing)
// This is the core idea behind DEPENDENCY INJECTION - making things completely modular and independent.
class ProductRepository(private val productDao: ProductDao) {

    val readAllData: LiveData<List<ProductModel>> = productDao.getProducts()

    suspend fun addProduct(productModel: ProductModel) {
        productDao.addProduct(productModel)
    }

    fun getProducts() = productDao.getProducts()

    suspend fun editProduct(newProductModel: ProductModel) = productDao.editProduct(newProductModel)

    suspend fun deleteProduct(productModel: ProductModel) = productDao.deleteProduct(productModel)


    companion object {
        // Singleton instantiation you already know and love
        @Volatile private var instance: ProductRepository? = null

        fun getInstance(productDao: ProductDao) =
            instance ?: synchronized(this) {
                instance
                        ?: ProductRepository(productDao).also { instance = it }
            }
    }
}