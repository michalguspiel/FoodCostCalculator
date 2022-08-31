package com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.editHalfProductDialogFragment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.erdees.foodcostcalc.data.AppRoomDataBase
import com.erdees.foodcostcalc.data.basic.BasicDataBase
import com.erdees.foodcostcalc.data.basic.BasicRepository
import com.erdees.foodcostcalc.data.halfProductIncludedInDish.HalfProductIncludedInDishRepository
import com.erdees.foodcostcalc.data.halfProductWithProductsIncluded.HalfProductWithProductsIncludedRepository
import com.erdees.foodcostcalc.data.halfproduct.HalfProductRepository
import com.erdees.foodcostcalc.data.productIncludedInHalfProduct.ProductIncludedInHalfProductRepository

/**TODO REFACTORING INTO VIEW BINDING +- MVVM PATTERN IMPROVEMENT */

class EditHalfProductFragmentViewModel(application: Application) : AndroidViewModel(application) {

  private val halfProductRepository: HalfProductRepository
  private val halfProductWithProductsIncludedRepository: HalfProductWithProductsIncludedRepository
  private val productIncludedInHalfProductRepository: ProductIncludedInHalfProductRepository
  private val halfProductIncludedInDishRepository : HalfProductIncludedInDishRepository

  init {
    val halfProductDao = AppRoomDataBase.getDatabase(application).halfProductDao()
    val halfProductWithProductIncludedDao =
      AppRoomDataBase.getDatabase(application).halfProductWithProductsIncludedDao()
    val productIncludedInHalfProductDao =
      AppRoomDataBase.getDatabase(application).productIncludedInHalfProductDao()
    val halfProductIncludedInDishDao =
      AppRoomDataBase.getDatabase(application).halfProductIncludedInDishDao()
    halfProductRepository = HalfProductRepository(halfProductDao)
    halfProductWithProductsIncludedRepository =
      HalfProductWithProductsIncludedRepository(halfProductWithProductIncludedDao)
    productIncludedInHalfProductRepository =
      ProductIncludedInHalfProductRepository(productIncludedInHalfProductDao)
    halfProductIncludedInDishRepository =
      HalfProductIncludedInDishRepository(halfProductIncludedInDishDao)
  }

  fun getHalfProductWithProductIncluded() = halfProductWithProductsIncludedRepository.readAllData

  fun getProductsIncludedFromHalfProduct(halfProductId: Long) =
    productIncludedInHalfProductRepository.getProductsIncludedFromHalfProduct(halfProductId)

  fun deleteHalfProduct(id: Long){
    halfProductRepository.deleteHalfProduct(id)
    productIncludedInHalfProductRepository.deleteProductsIncludedInHalfProduct(id)
    halfProductIncludedInDishRepository.deleteAllHalfProductsIncludedInDish(id)
  }
}
