package com.erdees.foodcostcalc.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.AppRoomDataBase
import com.erdees.foodcostcalc.data.basic.BasicDataBase
import com.erdees.foodcostcalc.data.basic.BasicRepository
import com.erdees.foodcostcalc.data.product.ProductRepository
import com.erdees.foodcostcalc.data.productIncluded.ProductIncludedRepository
import com.erdees.foodcostcalc.data.productIncludedInHalfProduct.ProductIncludedInHalfProductRepository
import com.erdees.foodcostcalc.model.Product
import com.erdees.foodcostcalc.model.ProductIncluded
import com.erdees.foodcostcalc.model.ProductIncludedInHalfProduct
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditProductViewModel(application: Application): AndroidViewModel(application) {

    private val productRepository: ProductRepository
    private val basicRepository: BasicRepository
    private val productIncludedRepository : ProductIncludedRepository
    private val productIncludedInHalfProductRepository : ProductIncludedInHalfProductRepository

    init {
        val productDao = AppRoomDataBase.getDatabase(application).productDao()
        val basicDao = BasicDataBase.getInstance().basicDao
        val productIncludedDao = AppRoomDataBase.getDatabase(application).productIncludedDao()
        val productIncludedInHalfProductDao = AppRoomDataBase.getDatabase(application).productIncludedInHalfProductDao()

        productRepository = ProductRepository(productDao)
        basicRepository = BasicRepository(basicDao)
        productIncludedRepository = ProductIncludedRepository(productIncludedDao)
        productIncludedInHalfProductRepository = ProductIncludedInHalfProductRepository(productIncludedInHalfProductDao)

    }
    /**Product repository methods*/
    fun getProducts() = productRepository.getProducts()

    fun editProduct(newProduct: Product) {
        viewModelScope.launch(Dispatchers.IO) {
            productRepository.editProduct(newProduct)
        }
    }

    /**Product included repository methods*/
    fun getCertainProductsIncluded(id: Long) = productIncludedRepository.getCertainProductIncluded(id)

    fun editProductsIncluded(productIncluded: ProductIncluded) {
        viewModelScope.launch(Dispatchers.IO) {
            productIncludedRepository.editProductsIncluded(productIncluded)
        }
    }

    /**ProductIncluded in Half Product Repository methods*/
    fun getCertainProductsIncludedInHalfProduct(productId: Long)
            = productIncludedInHalfProductRepository.getCertainProductsIncluded(productId)


    fun editProductIncludedInHalfProduct(productIncludedInHalfProduct: ProductIncludedInHalfProduct){
        viewModelScope.launch(Dispatchers.IO) {
            productIncludedInHalfProductRepository.editProductIncludedInHalfProduct(productIncludedInHalfProduct)
        }
    }


    /**Basic Repository methods*/

    fun setPosition(pos: Int) {
        basicRepository.setPosition(pos)
    }

    fun setFlag(boolean: Boolean) {
        basicRepository.setFlag(boolean)
    }

    fun getFlag() = basicRepository.getFlag()


}