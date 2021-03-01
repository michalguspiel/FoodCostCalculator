package com.example.foodcostcalc.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.foodcostcalc.data.AppRoomDataBase
import com.example.foodcostcalc.data.halfproduct.HalfProductRepository
import com.example.foodcostcalc.data.halfProductWithProductsIncluded.HalfProductWithProductsIncludedRepository
import com.example.foodcostcalc.data.productIncludedInHalfProduct.ProductIncludedInHalfProductRepository
import com.example.foodcostcalc.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HalfProductsViewModel(application: Application) : AndroidViewModel(application) {

    val readAllHalfProductData: LiveData<List<HalfProduct>>
    val readAllProductIncludedInHalfProductData: LiveData<List<ProductIncludedInHalfProduct>>
    val readAllProductIncludedInHalfProductDataNotAsc: LiveData<List<ProductIncludedInHalfProduct>>

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
     //   readAllHalfProductWithProductsIncludedData = halfProductWithProductsIncludedRepository.readAllData
    }


    fun getHalfProductsFromDish(dishId: Long) = halfProductRepository.getHalfProductsFromDish(dishId)

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


    fun addHalfProductIncludedInDish(halfProductIncludedInDish: HalfProductIncludedInDish){
        viewModelScope.launch(Dispatchers.IO){
            halfProductRepository
                .addHalfProductIncludedInDish(halfProductIncludedInDish)
        }
    }

    fun editHalfProductIncludedInDish(halfProductIncludedInDish: HalfProductIncludedInDish){
        viewModelScope.launch(Dispatchers.IO){
            halfProductRepository
                .editHalfProductIncludedInDish(halfProductIncludedInDish)
        }

    } fun deleteHalfProductIncludedInDish(halfProductIncludedInDish: HalfProductIncludedInDish){
        viewModelScope.launch(Dispatchers.IO){
            halfProductRepository
                .deleteHalfProductIncludedInDish(halfProductIncludedInDish)
        }
    }


    fun getCertainHalfProductWithProductsIncluded(halfProductId: Long)
    = halfProductWithProductsIncludedRepository.getCertainHalfProductWithProductsIncluded(halfProductId)


    fun getHalfProductWithProductIncluded() = halfProductWithProductsIncludedRepository.readAllData

    fun getProductsIncludedFromHalfProduct(halfProductId: Long)
    = productIncludedInHalfProductRepository.getProductsIncludedFromHalfProduct(halfProductId)





}
