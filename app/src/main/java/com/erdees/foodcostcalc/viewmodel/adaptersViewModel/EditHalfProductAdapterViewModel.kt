package com.erdees.foodcostcalc.viewmodel.adaptersViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.AppRoomDataBase
import com.erdees.foodcostcalc.data.basic.BasicDataBase
import com.erdees.foodcostcalc.data.basic.BasicRepository
import com.erdees.foodcostcalc.data.halfProductIncludedInDish.HalfProductIncludedInDishRepository
import com.erdees.foodcostcalc.data.halfproduct.HalfProductRepository
import com.erdees.foodcostcalc.data.productIncludedInHalfProduct.ProductIncludedInHalfProductRepository
import com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.models.HalfProductIncludedInDishModel
import com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.models.HalfProductModel
import com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.models.ProductIncludedInHalfProductModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditHalfProductAdapterViewModel(application: Application):AndroidViewModel(application) {

    val halfProductRepository: HalfProductRepository
    val halfProductIncludedInDishRepository: HalfProductIncludedInDishRepository
    val productIncludedInHalfProductRepository: ProductIncludedInHalfProductRepository
    val basicRepository: BasicRepository

    init {
        val halfProductDao = AppRoomDataBase.getDatabase(application).halfProductDao()
        val halfProductIncludedInDishDao =
            AppRoomDataBase.getDatabase(application).halfProductIncludedInDishDao()
        val productIncludedInHalfProductDao =
            AppRoomDataBase.getDatabase(application).productIncludedInHalfProductDao()
        val basicDao = BasicDataBase.getInstance().basicDao

        halfProductRepository = HalfProductRepository(halfProductDao)
        halfProductIncludedInDishRepository =
            HalfProductIncludedInDishRepository(halfProductIncludedInDishDao)
        productIncludedInHalfProductRepository =
            ProductIncludedInHalfProductRepository(productIncludedInHalfProductDao)
        basicRepository = BasicRepository(basicDao)
    }


    fun setProductIncludedInHalfProduct(productModel: ProductIncludedInHalfProductModel) {
        basicRepository.setProductIncludedInHalfProduct(productModel)
    }

    fun editHalfProducts(halfProductModel: HalfProductModel) {
        viewModelScope.launch(Dispatchers.IO) {
            halfProductRepository.editHalfProduct(halfProductModel)
        }
    }

    fun editProductIncludedInHalfProduct(productIncludedInHalfProductModel: ProductIncludedInHalfProductModel) {
        viewModelScope.launch(Dispatchers.IO) {
            productIncludedInHalfProductRepository.editProductIncludedInHalfProduct(
                productIncludedInHalfProductModel
            )
        }
    }

    fun getHalfProductsIncludedInDishFromDishByHalfProduct(productId: Long) =
        halfProductIncludedInDishRepository.getHalfProductsIncludedInDishFromDishByHalfProduct(
            productId
        )


    fun editHalfProductIncludedInDish(halfProductIncludedInDishModel: HalfProductIncludedInDishModel) {
        viewModelScope.launch(Dispatchers.IO) {
            halfProductIncludedInDishRepository
                .editHalfProductIncludedInDish(halfProductIncludedInDishModel)
        }

    }
}