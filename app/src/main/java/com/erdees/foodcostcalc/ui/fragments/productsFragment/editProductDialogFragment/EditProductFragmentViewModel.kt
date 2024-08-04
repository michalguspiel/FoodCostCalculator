package com.erdees.foodcostcalc.ui.fragments.productsFragment.editProductDialogFragment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.AppRoomDataBase
import com.erdees.foodcostcalc.data.product.ProductRepository
import com.erdees.foodcostcalc.entities.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditProductFragmentViewModel(application: Application) : AndroidViewModel(application) {

  private val productDao = AppRoomDataBase.getDatabase(application).productDao()
  private val productRepository: ProductRepository = ProductRepository.getInstance(productDao)

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
}
