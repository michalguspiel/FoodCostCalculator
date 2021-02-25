package com.example.foodcostcalc.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.foodcostcalc.data.AppRoomDataBase
import com.example.foodcostcalc.data.HalfProductRepository
import com.example.foodcostcalc.data.HalfProductWithProductsIncludedRepository
import com.example.foodcostcalc.data.ProductIncludedInHalfProductRepository
import com.example.foodcostcalc.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HalfProductsViewModel(application: Application) : AndroidViewModel(application) {

    val readAllHalfProductData: LiveData<List<HalfProduct>>
    val readAllProductIncludedInHalfProductData: LiveData<List<ProductIncludedInHalfProduct>>
    val readAllProductIncludedInHalfProductDataNotAsc: LiveData<List<ProductIncludedInHalfProduct>>
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
        readAllProductIncludedInHalfProductDataNotAsc = productIncludedInHalfProductRepository.readAllDataNotAsc
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

    fun addHalfProductWithProductsIncludedCrossRef(halfProductWithProductsIncludedCrossRef: HalfProductWithProductsIncludedCrossRef){
        viewModelScope.launch(Dispatchers.IO) {
            halfProductWithProductsIncludedRepository
                .addHalfProductWithProductsIncludedCrossRef(halfProductWithProductsIncludedCrossRef)
        }
    }

    fun addHalfProductToDish(dishWithHalfProductCrossRef: DishWithHalfProductCrossRef){
        viewModelScope.launch(Dispatchers.IO){
            halfProductWithProductsIncludedRepository
                .addHalfProductToDish(dishWithHalfProductCrossRef)
        }
    }

    fun getHalfProductWithProductIncluded() = halfProductWithProductsIncludedRepository.readAllData

    fun getProductsIncludedFromHalfProduct(halfProductId: Long)
    = productIncludedInHalfProductRepository.getProductsIncludedFromHalfProduct(halfProductId)





}
