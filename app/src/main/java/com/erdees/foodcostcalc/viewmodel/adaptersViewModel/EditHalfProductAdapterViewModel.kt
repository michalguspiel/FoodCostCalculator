package com.erdees.foodcostcalc.viewmodel.adaptersViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.AppRoomDataBase
import com.erdees.foodcostcalc.data.halfProductIncludedInDish.HalfProductIncludedInDishRepository
import com.erdees.foodcostcalc.data.halfproduct.HalfProductRepository
import com.erdees.foodcostcalc.data.productIncludedInHalfProduct.ProductIncludedInHalfProductRepository
import com.erdees.foodcostcalc.domain.model.halfProduct.HalfProductIncludedInDishModel
import com.erdees.foodcostcalc.domain.model.halfProduct.HalfProductModel
import com.erdees.foodcostcalc.domain.model.halfProduct.ProductIncludedInHalfProduct
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditHalfProductAdapterViewModel(application: Application):AndroidViewModel(application) {

    val halfProductRepository: HalfProductRepository
    val halfProductIncludedInDishRepository: HalfProductIncludedInDishRepository
    val productIncludedInHalfProductRepository: ProductIncludedInHalfProductRepository

    init {
        val halfProductDao = AppRoomDataBase.getDatabase(application).halfProductDao()
        val halfProductIncludedInDishDao =
            AppRoomDataBase.getDatabase(application).halfProductIncludedInDishDao()
        val productIncludedInHalfProductDao =
            AppRoomDataBase.getDatabase(application).productIncludedInHalfProductDao()

        halfProductRepository = HalfProductRepository(halfProductDao)
        halfProductIncludedInDishRepository =
            HalfProductIncludedInDishRepository(halfProductIncludedInDishDao)
        productIncludedInHalfProductRepository =
            ProductIncludedInHalfProductRepository(productIncludedInHalfProductDao)
    }


  fun editHalfProducts(halfProductModel: HalfProductModel) {
        viewModelScope.launch(Dispatchers.IO) {
            halfProductRepository.editHalfProduct(halfProductModel)
        }
    }

    fun editProductIncludedInHalfProduct(productIncludedInHalfProduct: ProductIncludedInHalfProduct) {
        viewModelScope.launch(Dispatchers.IO) {
            productIncludedInHalfProductRepository.editProductIncludedInHalfProduct(
                productIncludedInHalfProduct
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

  fun deleteProductIncludedInHalfProduct(productIncludedInHalfProduct: ProductIncludedInHalfProduct) {
    viewModelScope.launch(Dispatchers.IO) {
      productIncludedInHalfProductRepository.deleteProductIncludedInHalfProduct(
        productIncludedInHalfProduct
      )
    }
  }
}
