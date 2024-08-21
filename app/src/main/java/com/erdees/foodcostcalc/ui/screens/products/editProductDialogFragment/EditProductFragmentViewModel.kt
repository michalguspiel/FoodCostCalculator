package com.erdees.foodcostcalc.ui.screens.products.editProductDialogFragment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.model.ProductBase
import com.erdees.foodcostcalc.data.repository.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class EditProductFragmentViewModel : ViewModel(), KoinComponent {

  private val productRepository: ProductRepository by inject()

  /**ProductModel Repository methods*/
  fun deleteProduct(product: ProductBase) {
    viewModelScope.launch(Dispatchers.IO) {
      productRepository.deleteProduct(product)
    }
  }

  fun editProduct(newProduct: ProductBase) {
    viewModelScope.launch(Dispatchers.IO) {
      productRepository.editProduct(newProduct)
    }
  }
}
