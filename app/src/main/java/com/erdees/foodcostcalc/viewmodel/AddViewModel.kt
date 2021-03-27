package com.erdees.foodcostcalc.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.*
import com.erdees.foodcostcalc.data.basic.BasicDataBase
import com.erdees.foodcostcalc.data.basic.BasicRepository
import com.erdees.foodcostcalc.data.dish.DishRepository
import com.erdees.foodcostcalc.data.product.ProductRepository
import com.erdees.foodcostcalc.model.*
import kotlinx.coroutines.*

class AddViewModel(application: Application)
    : AndroidViewModel(application) {

    private val productRepository: ProductRepository

    init {
        val productDao = AppRoomDataBase.getDatabase(application).productDao()
        productRepository = ProductRepository(productDao)

    }

    fun addProducts(product: Product) {
        viewModelScope.launch(Dispatchers.IO) {
            productRepository.addProduct(product)
        }
    }

}