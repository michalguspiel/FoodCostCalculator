package com.example.foodcostcalc.data

import androidx.lifecycle.LiveData
import com.example.foodcostcalc.model.Product
import com.example.foodcostcalc.model.ProductIncluded

// Dao must be passed in - it is a dependency
// You could also instantiate the DAO right inside the class without all the fuss, right?
// No. This would break testability - you need to be able to pass a mock version of a DAO
// to the repository (e.g. one that upon calling getProducts() returns a dummy list of products for testing)
// This is the core idea behind DEPENDENCY INJECTION - making things completely modular and independent.
class ProductRepository(private val productDao: ProductDao) {

    val readAllData: LiveData<List<Product>> = productDao.getProducts()

    suspend fun addProduct(product: Product) {
        productDao.addProduct(product)
    }
    fun getProducts() = productDao.getProducts()

    fun getProduct(id: Long) = productDao.getProduct(id)

    suspend fun editProduct(newProduct: Product) = productDao.editProduct(newProduct)

    suspend fun deleteProduct(product: Product) = productDao.deleteProduct(product)

    suspend fun addProductToDish(product: ProductIncluded) = productDao.addProductToDish(product)



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