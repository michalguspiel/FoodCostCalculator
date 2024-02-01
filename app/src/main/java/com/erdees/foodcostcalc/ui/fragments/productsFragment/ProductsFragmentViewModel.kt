package com.erdees.foodcostcalc.ui.fragments.productsFragment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.erdees.foodcostcalc.data.AppRoomDataBase
import com.erdees.foodcostcalc.data.searchengine.SearchEngineRepository
import com.erdees.foodcostcalc.data.product.ProductRepository
import com.erdees.foodcostcalc.entities.Product

class ProductsFragmentViewModel(application: Application) : AndroidViewModel(application) {

    private val productRepository: ProductRepository
    private val readAllProductData: LiveData<List<Product>>
    private val searchEngineRepository: SearchEngineRepository = SearchEngineRepository.getInstance()

    init {
        val productDao = AppRoomDataBase.getDatabase(application).productDao()
        productRepository = ProductRepository.getInstance(productDao)
        readAllProductData = productRepository.readAllData
    }

    fun getProducts() = productRepository.getProducts()

    fun getWhatToSearchFor() = searchEngineRepository.getWhatToSearchFor()
}
