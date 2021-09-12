package com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.editHalfProductDialogFragment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.erdees.foodcostcalc.data.AppRoomDataBase
import com.erdees.foodcostcalc.data.basic.BasicDataBase
import com.erdees.foodcostcalc.data.basic.BasicRepository
import com.erdees.foodcostcalc.data.halfProductWithProductsIncluded.HalfProductWithProductsIncludedRepository
import com.erdees.foodcostcalc.data.halfproduct.HalfProductRepository
import com.erdees.foodcostcalc.data.productIncludedInHalfProduct.ProductIncludedInHalfProductRepository

/**TODO REFACTORING INTO VIEW BINDING + MVVM PATTERN IMPROVEMENT */


class EditHalfProductFragmentViewModel(application: Application) : AndroidViewModel(application) {

    private val halfProductRepository: HalfProductRepository
    private val basicRepository: BasicRepository
    private val halfProductWithProductsIncludedRepository: HalfProductWithProductsIncludedRepository
    private val productIncludedInHalfProductRepository: ProductIncludedInHalfProductRepository

    init {
        val basicDao = BasicDataBase.getInstance().basicDao
        val halfProductDao = AppRoomDataBase.getDatabase(application).halfProductDao()
        val halfProductWithProductIncludedDao =
            AppRoomDataBase.getDatabase(application).halfProductWithProductsIncludedDao()
        val productIncludedInHalfProductDao =
            AppRoomDataBase.getDatabase(application).productIncludedInHalfProductDao()

        halfProductRepository = HalfProductRepository(halfProductDao)
        basicRepository = BasicRepository(basicDao)
        halfProductWithProductsIncludedRepository = HalfProductWithProductsIncludedRepository(halfProductWithProductIncludedDao)
        productIncludedInHalfProductRepository =
            ProductIncludedInHalfProductRepository(productIncludedInHalfProductDao)
    }

    fun getHalfProducts() = halfProductRepository.readAllData

    fun getHalfProductWithProductIncluded() = halfProductWithProductsIncludedRepository.readAllData

    fun getProductsIncludedFromHalfProduct(halfProductId: Long) = productIncludedInHalfProductRepository.getProductsIncludedFromHalfProduct(halfProductId)

    fun setPosition(pos: Int) {
        basicRepository.setPosition(pos)
    }

    fun getFlag() = basicRepository.getFlag()

    fun setFlag(boolean: Boolean) {
        basicRepository.setFlag(boolean)
    }

}