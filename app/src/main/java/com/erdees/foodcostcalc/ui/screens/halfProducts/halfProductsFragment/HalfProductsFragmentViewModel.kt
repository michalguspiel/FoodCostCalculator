package com.erdees.foodcostcalc.ui.screens.halfProducts.halfProductsFragment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.repository.HalfProductRepository
import com.erdees.foodcostcalc.data.searchengine.SearchEngineRepository
import com.erdees.foodcostcalc.domain.mapper.Mapper.toDishDomain
import com.erdees.foodcostcalc.domain.mapper.Mapper.toHalfProductDomain
import com.erdees.foodcostcalc.domain.model.dish.DishDomain
import com.erdees.foodcostcalc.domain.model.halfProduct.HalfProductDomain
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class HalfProductsFragmentViewModel : ViewModel(), KoinComponent {

  private val searchEngineRepository: SearchEngineRepository = SearchEngineRepository.getInstance()
  private val halfProductRepository: HalfProductRepository by inject()


  private val searchedKey = searchEngineRepository.getWhatToSearchFor().asFlow().stateIn(
    viewModelScope,
    SharingStarted.Lazily, ""
  )

  private val _halfProducts = halfProductRepository.completeHalfProducts.map {
    it.map { completeHalfProduct -> completeHalfProduct.toHalfProductDomain() }
  }.stateIn(
    viewModelScope,
    SharingStarted.Lazily,
    emptyList()
  )

  val halfProducts: StateFlow<List<HalfProductDomain>> =
    combine(searchedKey, _halfProducts) { key, halfProducts ->
      halfProducts.filter {
        it.name.lowercase().contains(key.lowercase())
      }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())


  val idToQuantityMap = mutableMapOf<Long, Double>()
  val expandedList = mutableListOf<Long>()

  fun determineIfCardIsExpanded(id: Long): Boolean {
    return expandedList.contains(id)
  }

  fun getWhatToSearchFor() = searchEngineRepository.getWhatToSearchFor()
}
