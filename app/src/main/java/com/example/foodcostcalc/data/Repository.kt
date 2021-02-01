package com.example.foodcostcalc.data

import com.example.foodcostcalc.model.Dish
import com.example.foodcostcalc.model.Product

// Dao must be passed in - it is a dependency
// You could also instantiate the DAO right inside the class without all the fuss, right?
// No. This would break testability - you need to be able to pass a mock version of a DAO
// to the repository (e.g. one that upon calling getProducts() returns a dummy list of products for testing)
// This is the core idea behind DEPENDENCY INJECTION - making things completely modular and independent.
class Repository private constructor(private val productDao: ProductDao) {

    // This may seem redundant.
    // Imagine a code which also updates and checks the backend.
    fun addProduct(product: Product) {
        productDao.addProduct(product)
    }
    fun getProduct() = productDao.getProducts()

    fun addDish(dish: Dish){
        productDao.addDish(dish)
    }
    fun getDishes() = productDao.getDishes()

    fun addProductToDish(dish: Dish, product: Product, weight: Double) {productDao.addProductToDish(dish,product,weight)}


    fun editProduct(newProduct: Product, oldProduct: Product) = productDao.editProduct(newProduct,oldProduct)

    fun deleteProduct(product: Product) = productDao.deleteProduct(product)

    fun setPosition(pos: Int) = productDao.setPosition(pos)

    fun getPosition() = productDao.getPosition()

    fun setSecondPosition(pos: Int) = productDao.setSecondPosition(pos)

    fun getSecondPosition() = productDao.getSecondPosition()

    fun setFlag(boolean: Boolean) = productDao.setFlag(boolean)

    fun getFlag() = productDao.getFlag()

    fun editDish(dish: Dish,listOfProducts: MutableList<Pair<Product, Double>>) = productDao.editDish(dish,listOfProducts)

    fun deleteDish(dish: Dish) = productDao.deleteDish(dish)


    fun deleteProductFromDish(dish:Dish ,product: Product) = productDao.deleteProductFromDish(dish,product)

    companion object {
        // Singleton instantiation you already know and love
        @Volatile private var instance: Repository? = null

        fun getInstance(productDao: ProductDao) =
            instance ?: synchronized(this) {
                instance
                        ?: Repository(productDao).also { instance = it }
            }
    }
}