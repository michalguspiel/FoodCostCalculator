package com.erdees.foodcostcalc.ui.fragments.halfProductsFragment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.erdees.foodcostcalc.data.AppRoomDataBase
import com.erdees.foodcostcalc.data.searchengine.SearchEngineRepository
import com.erdees.foodcostcalc.data.halfProductWithProductsIncluded.HalfProductWithProductsIncludedRepository

class HalfProductsFragmentViewModel(application: Application) : AndroidViewModel(application) {

  private val halfProductWithProductsIncludedRepository: HalfProductWithProductsIncludedRepository
  private val searchEngineRepository: SearchEngineRepository = SearchEngineRepository.getInstance()
  val idToQuantityMap = mutableMapOf<Long, Double>()
  val expandedList = mutableListOf<Long>()

  init {
    val halfProductWithProductsIncludedDao =
      AppRoomDataBase.getDatabase(application).halfProductWithProductsIncludedDao()
    halfProductWithProductsIncludedRepository =
      HalfProductWithProductsIncludedRepository(halfProductWithProductsIncludedDao)
  }

  fun determineIfCardIsExpanded(id: Long): Boolean {
  return expandedList.contains(id)
  }

  fun getHalfProductWithProductIncluded() = halfProductWithProductsIncludedRepository.readAllData

  fun getWhatToSearchFor() = searchEngineRepository.getWhatToSearchFor()
}
