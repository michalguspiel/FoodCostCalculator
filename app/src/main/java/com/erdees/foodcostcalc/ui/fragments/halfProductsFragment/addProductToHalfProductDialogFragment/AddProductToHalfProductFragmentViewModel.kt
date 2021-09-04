package com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.addProductToHalfProductDialogFragment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.AppRoomDataBase
import com.erdees.foodcostcalc.data.basic.BasicDataBase
import com.erdees.foodcostcalc.data.basic.BasicRepository
import com.erdees.foodcostcalc.data.halfproduct.HalfProductRepository
import com.erdees.foodcostcalc.data.product.ProductRepository
import com.erdees.foodcostcalc.data.productIncludedInHalfProduct.ProductIncludedInHalfProductRepository
import com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.models.HalfProductModel
import com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.models.ProductIncludedInHalfProductModel
import com.erdees.foodcostcalc.ui.fragments.productsFragment.models.ProductModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddProductToHalfProductFragmentViewModel(application: Application) :
    AndroidViewModel(application) {

    val readAllHalfProductModelData: LiveData<List<HalfProductModel>>
    val readAllProductModelData: LiveData<List<ProductModel>>

    private val basicRepository: BasicRepository
    private val productRepository: ProductRepository
    private val halfProductRepository: HalfProductRepository
    private val productIncludedInHalfProductRepository: ProductIncludedInHalfProductRepository


    init {
        val basicDao = BasicDataBase.getInstance().basicDao
        val productDao = AppRoomDataBase.getDatabase(application).productDao()
        val halfProductDao = AppRoomDataBase.getDatabase(application).halfProductDao()
        val productIncludedInHalfProductDao =
            AppRoomDataBase.getDatabase(application).productIncludedInHalfProductDao()

        basicRepository = BasicRepository(basicDao)
        halfProductRepository = HalfProductRepository(halfProductDao)
        productRepository = ProductRepository(productDao)
        productIncludedInHalfProductRepository =
            ProductIncludedInHalfProductRepository((productIncludedInHalfProductDao))

        readAllHalfProductModelData = halfProductRepository.readAllData
        readAllProductModelData = productRepository.readAllData

    }

    fun getHalfProductToDialog() = basicRepository.getHalfProductToDialog()

    fun addProductIncludedInHalfProduct(productIncludedInHalfProductModel: ProductIncludedInHalfProductModel) {
        viewModelScope.launch(Dispatchers.IO) {
            productIncludedInHalfProductRepository.addProductIncludedInHalfProduct(
                productIncludedInHalfProductModel
            )
        }

    }


}
