package com.erdees.foodcostcalc.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.AppRoomDataBase
import com.erdees.foodcostcalc.data.dish.DishRepository
import com.erdees.foodcostcalc.data.halfproduct.HalfProductRepository
import com.erdees.foodcostcalc.data.product.ProductRepository
import com.erdees.foodcostcalc.data.productIncludedInHalfProduct.ProductIncludedInHalfProductRepository
import com.erdees.foodcostcalc.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddProductToHalfProductViewModel(application: Application): AndroidViewModel(application) {

    val readAllHalfProductData: LiveData<List<HalfProduct>>
    val readAllProductData: LiveData<List<Product>>

    private val productRepository: ProductRepository
    private val halfProductRepository : HalfProductRepository
    private val productIncludedInHalfProductRepository : ProductIncludedInHalfProductRepository


    init {
        val productDao = AppRoomDataBase.getDatabase(application).productDao()
        val halfProductDao = AppRoomDataBase.getDatabase(application).halfProductDao()
        val productIncludedInHalfProductDao = AppRoomDataBase.getDatabase(application).productIncludedInHalfProductDao()

        halfProductRepository = HalfProductRepository(halfProductDao)
        productRepository = ProductRepository(productDao)
        productIncludedInHalfProductRepository = ProductIncludedInHalfProductRepository((productIncludedInHalfProductDao))

        readAllHalfProductData = halfProductRepository.readAllData
        readAllProductData = productRepository.readAllData

    }


    fun addProductIncludedInHalfProduct(productIncludedInHalfProduct: ProductIncludedInHalfProduct){
        viewModelScope.launch(Dispatchers.IO) {
            productIncludedInHalfProductRepository.addProductIncludedInHalfProduct(productIncludedInHalfProduct)
        }

    }


}
