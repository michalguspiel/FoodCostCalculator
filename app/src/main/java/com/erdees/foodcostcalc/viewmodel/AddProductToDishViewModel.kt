package com.erdees.foodcostcalc.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.AppRoomDataBase
import com.erdees.foodcostcalc.data.dish.DishRepository
import com.erdees.foodcostcalc.data.halfProductIncludedInDish.HalfProductIncludedInDishRepository
import com.erdees.foodcostcalc.data.halfproduct.HalfProductRepository
import com.erdees.foodcostcalc.data.product.ProductRepository
import com.erdees.foodcostcalc.data.productIncluded.ProductIncludedRepository
import com.erdees.foodcostcalc.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddProductToDishViewModel(application: Application) : AndroidViewModel(application) {


    val readAllHalfProductData: LiveData<List<HalfProduct>>
    val readAllProductData: LiveData<List<Product>>
    val readAllDishData: LiveData<List<Dish>>

    private val productRepository: ProductRepository
    private val halfProductRepository : HalfProductRepository
    private val dishRepository: DishRepository
    private val productIncludedRepository: ProductIncludedRepository
    private val halfProductIncludedInDishRepository : HalfProductIncludedInDishRepository

    init {
        val productDao = AppRoomDataBase.getDatabase(application).productDao()
        val halfProductDao = AppRoomDataBase.getDatabase(application).halfProductDao()
        val dishDao = AppRoomDataBase.getDatabase(application).dishDao()
        val productIncludedDao = AppRoomDataBase.getDatabase(application).productIncludedDao()
        val halfProductIncludedInDishDao = AppRoomDataBase.getDatabase(application).halfProductIncludedInDishDao()


        halfProductRepository = HalfProductRepository(halfProductDao)
        productRepository = ProductRepository(productDao)
        dishRepository = DishRepository(dishDao)
        productIncludedRepository = ProductIncludedRepository(productIncludedDao)
        halfProductIncludedInDishRepository = HalfProductIncludedInDishRepository(halfProductIncludedInDishDao)

        readAllHalfProductData = halfProductRepository.readAllData
        readAllProductData = productRepository.readAllData
        readAllDishData = dishRepository.readAllData

    }

    fun getHalfProducts() = halfProductRepository.readAllData

    fun addProductToDish(product: ProductIncluded) {
        viewModelScope.launch(Dispatchers.IO) {
            productIncludedRepository.addProductToDish(product)
        }
    }

    fun addHalfProductIncludedInDish(halfProductIncludedInDish: HalfProductIncludedInDish){
        viewModelScope.launch(Dispatchers.IO){
            halfProductIncludedInDishRepository
                .addHalfProductIncludedInDish(halfProductIncludedInDish)
        }
    }


}