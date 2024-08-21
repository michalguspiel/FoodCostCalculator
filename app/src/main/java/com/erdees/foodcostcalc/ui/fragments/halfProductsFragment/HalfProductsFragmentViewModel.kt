package com.erdees.foodcostcalc.ui.fragments.halfProductsFragment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.erdees.foodcostcalc.data.searchengine.SearchEngineRepository

class HalfProductsFragmentViewModel(application: Application) : AndroidViewModel(application) {

  private val searchEngineRepository: SearchEngineRepository = SearchEngineRepository.getInstance()
  val idToQuantityMap = mutableMapOf<Long, Double>()
  val expandedList = mutableListOf<Long>()

  fun determineIfCardIsExpanded(id: Long): Boolean {
    return expandedList.contains(id)
  }

  fun getWhatToSearchFor() = searchEngineRepository.getWhatToSearchFor()
}
