package com.erdees.foodcostcalc.ui.fragments.productsFragment.editProductDialogFragment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.AppRoomDataBase
import com.erdees.foodcostcalc.data.product.ProductRepository
import com.erdees.foodcostcalc.data.productIncluded.ProductIncludedRepository
import com.erdees.foodcostcalc.data.productIncludedInHalfProduct.ProductIncludedInHalfProductRepository
import com.erdees.foodcostcalc.entities.ProductIncludedInHalfProduct
import com.erdees.foodcostcalc.entities.ProductIncluded
import com.erdees.foodcostcalc.entities.Product
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

    productRepository = ProductRepository.getInstance(productDao)
    productIncludedRepository = ProductIncludedRepository.getInstance(productIncludedDao)
    productIncludedInHalfProductRepository =
      ProductIncludedInHalfProductRepository.getInstance(productIncludedInHalfProductDao)

  }
  /**ProductModel Repository methods*/
  fun deleteProduct(product: Product) {
    viewModelScope.launch(Dispatchers.IO) {
      productRepository.deleteProduct(product)
    }
  }

  fun editProduct(newProduct: Product) {
    viewModelScope.launch(Dispatchers.IO) {
      productRepository.editProduct(newProduct)
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
