package com.erdees.foodcostcalc.ui.screens.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.repository.ProductRepository
import com.erdees.foodcostcalc.data.searchengine.SearchEngineRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Locale

class ProductsFragmentViewModel : ViewModel(), KoinComponent {

  private val productRepository: ProductRepository by inject()
  private val searchEngineRepository: SearchEngineRepository = SearchEngineRepository.getInstance()

  private val products = productRepository.products.stateIn(
    scope = viewModelScope,
    started = SharingStarted.Lazily,
    initialValue = emptyList()
  )

  private val searchWord = searchEngineRepository.getWhatToSearchFor().asFlow().stateIn(
    scope = viewModelScope,
    started = SharingStarted.Lazily,
    initialValue = ""
  )

  val filteredProducts = combine(
    products,
    searchWord
  ) { products, searchWord ->
    products.filter {
      it.name.lowercase(Locale.getDefault()).contains(searchWord.lowercase())
    }
  }
}
