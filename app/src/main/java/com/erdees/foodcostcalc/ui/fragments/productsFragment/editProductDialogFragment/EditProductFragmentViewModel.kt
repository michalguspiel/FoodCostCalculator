package com.erdees.foodcostcalc.ui.fragments.productsFragment.editProductDialogFragment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.model.Product
import com.erdees.foodcostcalc.data.repository.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class EditProductFragmentViewModel : ViewModel(), KoinComponent {

  private val productRepository: ProductRepository by inject()

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
