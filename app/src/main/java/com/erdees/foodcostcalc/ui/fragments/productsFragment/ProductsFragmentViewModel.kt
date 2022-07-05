package com.erdees.foodcostcalc.ui.fragments.productsFragment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.erdees.foodcostcalc.data.AppRoomDataBase
import com.erdees.foodcostcalc.data.basic.BasicDataBase
import com.erdees.foodcostcalc.data.basic.BasicRepository
import com.erdees.foodcostcalc.data.product.ProductRepository
import com.erdees.foodcostcalc.ui.fragments.productsFragment.models.ProductModel

class ProductsFragmentViewModel(application: Application) : AndroidViewModel(application) {

    private val productRepository: ProductRepository
    private val readAllProductModelData: LiveData<List<ProductModel>>
    val basicRepository: BasicRepository

    init {
        val productDao = AppRoomDataBase.getDatabase(application).productDao()
        val basicDao = BasicDataBase.getInstance().basicDao

        basicRepository = BasicRepository(basicDao)
        productRepository = ProductRepository(productDao)
        readAllProductModelData = productRepository.readAllData

    }

    fun getProducts() = productRepository.getProducts()

    fun getWhatToSearchFor() = basicRepository.getWhatToSearchFor()
}
