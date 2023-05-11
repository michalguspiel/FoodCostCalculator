package com.erdees.foodcostcalc.ui.fragments.productsFragment.editProductDialogFragment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.AppRoomDataBase
import com.erdees.foodcostcalc.data.product.ProductRepository
import com.erdees.foodcostcalc.data.productIncluded.ProductIncludedRepository
import com.erdees.foodcostcalc.data.productIncludedInHalfProduct.ProductIncludedInHalfProductRepository
import com.erdees.foodcostcalc.domain.model.halfProduct.ProductIncludedInHalfProduct
import com.erdees.foodcostcalc.domain.model.product.ProductIncluded
import com.erdees.foodcostcalc.domain.model.product.ProductModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class EditProductFragmentViewModel(application: Application) : AndroidViewModel(application) {

  private val productRepository: ProductRepository
  private val productIncludedRepository: ProductIncludedRepository
  private val productIncludedInHalfProductRepository: ProductIncludedInHalfProductRepository

  init {
    val productDao = AppRoomDataBase.getDatabase(application).productDao()
    val productIncludedDao = AppRoomDataBase.getDatabase(application).productIncludedDao()
    val productIncludedInHalfProductDao =
      AppRoomDataBase.getDatabase(application).productIncludedInHalfProductDao()

    productRepository = ProductRepository(productDao)
    productIncludedRepository = ProductIncludedRepository(productIncludedDao)
    productIncludedInHalfProductRepository =
      ProductIncludedInHalfProductRepository(productIncludedInHalfProductDao)

  }
  /**ProductModel Repository methods*/
  fun deleteProduct(productModel: ProductModel) {
    viewModelScope.launch(Dispatchers.IO) {
      productRepository.deleteProduct(productModel)
    }
  }

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
  fun getCertainProductsIncludedInHalfProduct(productId: Long) =
    productIncludedInHalfProductRepository.getCertainProductsIncluded(productId)

  fun editProductIncludedInHalfProduct(productIncludedInHalfProduct: ProductIncludedInHalfProduct) {
    viewModelScope.launch(Dispatchers.IO) {
      productIncludedInHalfProductRepository.editProductIncludedInHalfProduct(
        productIncludedInHalfProduct
      )
    }
  }
}
