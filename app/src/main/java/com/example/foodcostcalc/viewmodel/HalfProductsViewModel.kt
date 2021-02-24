package com.example.foodcostcalc.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.foodcostcalc.data.AppRoomDataBase
import com.example.foodcostcalc.data.HalfProductRepository
import com.example.foodcostcalc.data.HalfProductWithProductsIncludedRepository
import com.example.foodcostcalc.data.ProductIncludedInHalfProductRepository
import com.example.foodcostcalc.model.HalfProduct
import com.example.foodcostcalc.model.HalfProductWithProductsIncluded
import com.example.foodcostcalc.model.ProductIncludedInHalfProduct
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HalfProductsViewModel(application: Application) : AndroidViewModel(application) {

    val readAllHalfProductData: LiveData<List<HalfProduct>>
    val readAllProductIncludedInHalfProductData: LiveData<List<ProductIncludedInHalfProduct>>
    val readAllHalfProductWithProductsIncludedData: LiveData<List<HalfProductWithProductsIncluded>>

    private val halfProductRepository: HalfProductRepository
    private val productIncludedInHalfProductRepository: ProductIncludedInHalfProductRepository
    private val halfProductWithProductsIncludedRepository: HalfProductWithProductsIncludedRepository

    init {
        val halfProductDao = AppRoomDataBase.getDatabase(application).halfProductDao()
        val productIncludedInHalfProductDao =
            AppRoomDataBase.getDatabase(application).productIncludedInHalfProductDao()
        val halfProductWithProductIncludedDao = AppRoomDataBase.getDatabase(application).halfProductWithProductsIncludedDao()
        halfProductRepository = HalfProductRepository(halfProductDao)
        productIncludedInHalfProductRepository =
            ProductIncludedInHalfProductRepository(productIncludedInHalfProductDao)
        halfProductWithProductsIncludedRepository = HalfProductWithProductsIncludedRepository(halfProductWithProductIncludedDao)


        readAllHalfProductData = halfProductRepository.readAllData
        readAllProductIncludedInHalfProductData = productIncludedInHalfProductRepository.readAllData
        readAllHalfProductWithProductsIncludedData = halfProductWithProductsIncludedRepository.readAllData
    }

    fun getHalfProducts() = halfProductRepository.readAllData

    fun addHalfProducts(halfProduct: HalfProduct) {
        viewModelScope.launch(Dispatchers.IO) {
            halfProductRepository.addHalfProduct(halfProduct)
        }
    }

    fun editHalfProducts(halfProduct: HalfProduct) {
        viewModelScope.launch(Dispatchers.IO) {
            halfProductRepository.editHalfProduct(halfProduct)
        }
    }

    fun deleteHalfProducts(halfProduct: HalfProduct) {
        viewModelScope.launch(Dispatchers.IO) {
            halfProductRepository.deleteHalfProduct(halfProduct)
        }

    }

    fun getProductsIncludedInHalfProduct() = productIncludedInHalfProductRepository.readAllData

    fun addProductIncludedInHalfProduct(productIncludedInHalfProduct: ProductIncludedInHalfProduct){
        viewModelScope.launch(Dispatchers.IO) {
            productIncludedInHalfProductRepository.addProductIncludedInHalfProduct(productIncludedInHalfProduct)
        }

    }
    fun editProductIncludedInHalfProduct(productIncludedInHalfProduct: ProductIncludedInHalfProduct){
        viewModelScope.launch(Dispatchers.IO) {
            productIncludedInHalfProductRepository.editProductIncludedInHalfProduct(productIncludedInHalfProduct)
        }

    }
    fun deleteProductIncludedInHalfProduct(productIncludedInHalfProduct: ProductIncludedInHalfProduct){
        viewModelScope.launch(Dispatchers.IO) {
            productIncludedInHalfProductRepository.deleteProductIncludedInHalfProduct(productIncludedInHalfProduct)
        }

    }

    fun getHalfProductWithProductIncluded() = halfProductWithProductsIncludedRepository.readAllData


}
