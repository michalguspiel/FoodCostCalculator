package com.erdees.foodcostcalc.ui.fragments.productsFragment.editProductDialogFragment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.AppRoomDataBase
import com.erdees.foodcostcalc.data.basic.BasicDataBase
import com.erdees.foodcostcalc.data.basic.BasicRepository
import com.erdees.foodcostcalc.data.product.ProductRepository
import com.erdees.foodcostcalc.data.productIncluded.ProductIncludedRepository
import com.erdees.foodcostcalc.data.productIncludedInHalfProduct.ProductIncludedInHalfProductRepository
import com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.models.ProductIncludedInHalfProductModel
import com.erdees.foodcostcalc.ui.fragments.productsFragment.models.ProductIncluded
import com.erdees.foodcostcalc.ui.fragments.productsFragment.models.ProductModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**TODO REFACTORING INTO VIEW BINDING + MVVM PATTERN IMPROVEMENT */


class EditProductFragmentViewModel(application: Application) : AndroidViewModel(application) {

    private val productRepository: ProductRepository
    private val basicRepository: BasicRepository
    private val productIncludedRepository: ProductIncludedRepository
    private val productIncludedInHalfProductRepository: ProductIncludedInHalfProductRepository

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

    /**ProductModel repository methods*/
    fun getProducts() = productRepository.getProducts()

    fun editProduct(newProductModel: ProductModel) {
        viewModelScope.launch(Dispatchers.IO) {
            productRepository.editProduct(newProductModel)
        }
    }

    /**ProductModel included repository methods*/
    fun getCertainProductsIncluded(id: Long) = productIncludedRepository.getCertainProductIncluded(id)

    fun editProductsIncluded(productIncluded: ProductIncluded) {
        viewModelScope.launch(Dispatchers.IO) {
            productIncludedRepository.editProductsIncluded(productIncluded)
        }
    }

    /**ProductIncluded in Half ProductModel Repository methods*/
    fun getCertainProductsIncludedInHalfProduct(productId: Long) = productIncludedInHalfProductRepository.getCertainProductsIncluded(productId)


    fun editProductIncludedInHalfProduct(productIncludedInHalfProductModel: ProductIncludedInHalfProductModel) {
        viewModelScope.launch(Dispatchers.IO) {
            productIncludedInHalfProductRepository.editProductIncludedInHalfProduct(
                productIncludedInHalfProductModel
            )
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