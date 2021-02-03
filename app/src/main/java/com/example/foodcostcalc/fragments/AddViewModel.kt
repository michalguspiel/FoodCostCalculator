package com.example.foodcostcalc.fragments

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.foodcostcalc.data.*
import com.example.foodcostcalc.model.Dish
import com.example.foodcostcalc.model.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddViewModel(application: Application)
    : AndroidViewModel(application) {

     val readAllProductData: LiveData<List<Product>>
     val readAllDishData : LiveData<List<Dish>>
    private val productRepository: ProductRepository
    private val dishRepository : DishRepository
    private val basicRepository : BasicRepository
    init {
        val productDao = AppRoomDataBase.getDatabase(application).productDao()
        val dishDao = AppRoomDataBase.getDatabase(application).dishDao()
        val basicDao = BasicDataBase.getInstance().basicDao
        productRepository = ProductRepository(productDao)
        dishRepository = DishRepository(dishDao)
        basicRepository = BasicRepository(basicDao)
        readAllProductData = productRepository.readAllData
        readAllDishData = dishRepository.readAllData
    }

    fun getProducts()                 = productRepository.getProducts()

    fun getProduct(id: Long)          = productRepository.getProduct(id)

    fun getDishes()                   = dishRepository.getDishes()



    fun addProducts(product: Product) {
   viewModelScope.launch(Dispatchers.IO){
       productRepository.addProduct(product)
   }
    }
    fun addDishes(dish: Dish) {
        viewModelScope.launch(Dispatchers.IO){
            dishRepository.addDish(dish)
        }
    }

    fun deleteDish(dish: Dish){
        viewModelScope.launch(Dispatchers.IO){
            dishRepository.deleteDish(dish)
        }
    }

    fun editProduct(newProduct: Product) {
        viewModelScope.launch(Dispatchers.IO){
            productRepository.editProduct(newProduct)
        }
    }
    fun deleteProduct(product: Product) {
        viewModelScope.launch(Dispatchers.IO) {
            productRepository.deleteProduct(product)
        }
    }

    fun addProductToDish(product: ProductIncluded){
        viewModelScope.launch(Dispatchers.IO){
            productRepository.addProductToDish(product)
        }
    }

    fun editDish(dish: Dish){
        viewModelScope.launch(Dispatchers.IO){
            dishRepository.editDish(dish)
        }
    }

    fun editProductsIncluded(productIncluded: ProductIncluded){
        viewModelScope.launch(Dispatchers.IO) {
            dishRepository.editProductsIncluded(productIncluded)
        }

    }

    fun getDishesWithProductsIncluded() = dishRepository.getDishesWithProductsIncluded()

    fun getIngredientsFromDish(dishId: Long) = dishRepository.getIngredientsFromDish(dishId)


    fun setPosition(pos: Int){
        basicRepository.setPosition(pos)
    }

    fun getPosition() = basicRepository.getPosition()

    fun setSecondPosition(pos: Int){
       basicRepository.setSecondPosition(pos)
    }

    fun getSecondPosition() = basicRepository.getSecondPosition()

    fun setFlag(boolean: Boolean) {
        basicRepository.setFlag(boolean)
    }

    fun getFlag() = basicRepository.getFlag()
}