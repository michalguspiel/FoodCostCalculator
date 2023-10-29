package com.erdees.foodcostcalc.ui.activities.mainActivity

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.erdees.foodcostcalc.data.searchengine.SearchEngineRepository

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

  private val searchEngineRepository = SearchEngineRepository.getInstance()
  fun searchFor(word: String) {
    searchEngineRepository.searchFor(word)
  }

  /**
   * This indicates the position of the fragment as per bottom navigation bar.
   */
  private var position: Int? = null
  fun getPosition(): Int? = position
  fun setPosition(newPosition: Int?) {
    position = newPosition
  }
}
