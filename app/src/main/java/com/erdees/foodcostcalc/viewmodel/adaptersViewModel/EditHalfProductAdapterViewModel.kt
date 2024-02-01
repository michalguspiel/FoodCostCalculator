package com.erdees.foodcostcalc.viewmodel.adaptersViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.AppRoomDataBase
import com.erdees.foodcostcalc.data.halfProductIncludedInDish.HalfProductIncludedInDishRepository
import com.erdees.foodcostcalc.data.halfproduct.HalfProductRepository
import com.erdees.foodcostcalc.data.productIncludedInHalfProduct.ProductIncludedInHalfProductRepository
import com.erdees.foodcostcalc.entities.HalfProductIncludedInDish
import com.erdees.foodcostcalc.entities.HalfProduct
import com.erdees.foodcostcalc.entities.ProductIncludedInHalfProduct
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

        halfProductRepository = HalfProductRepository.getInstance(halfProductDao)
        halfProductIncludedInDishRepository =
            HalfProductIncludedInDishRepository.getInstance(halfProductIncludedInDishDao)
        productIncludedInHalfProductRepository =
            ProductIncludedInHalfProductRepository.getInstance(productIncludedInHalfProductDao)
    }


  fun editHalfProducts(halfProduct: HalfProduct) {
        viewModelScope.launch(Dispatchers.IO) {
            halfProductRepository.editHalfProduct(halfProduct)
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


    fun editHalfProductIncludedInDish(halfProductIncludedInDish: HalfProductIncludedInDish) {
        viewModelScope.launch(Dispatchers.IO) {
            halfProductIncludedInDishRepository
                .editHalfProductIncludedInDish(halfProductIncludedInDish)
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
