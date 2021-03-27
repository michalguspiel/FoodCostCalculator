package com.erdees.foodcostcalc.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.erdees.foodcostcalc.data.AppRoomDataBase
import com.erdees.foodcostcalc.data.basic.BasicDataBase
import com.erdees.foodcostcalc.data.basic.BasicRepository
import com.erdees.foodcostcalc.data.grandDish.GrandDishRepository
import com.erdees.foodcostcalc.data.product.ProductRepository
import com.erdees.foodcostcalc.model.Product

class ProductsViewModel(application: Application):AndroidViewModel(application) {

    private val productRepository: ProductRepository
    val readAllProductData: LiveData<List<Product>>
    val basicRepository : BasicRepository

    init {
        val productDao = AppRoomDataBase.getDatabase(application).productDao()
        val basicDao = BasicDataBase.getInstance().basicDao

        basicRepository = BasicRepository(basicDao)
        productRepository = ProductRepository(productDao)

        readAllProductData = productRepository.readAllData

    }


    fun getWhatToSearchFor() = basicRepository.getWhatToSearchFor()
}
