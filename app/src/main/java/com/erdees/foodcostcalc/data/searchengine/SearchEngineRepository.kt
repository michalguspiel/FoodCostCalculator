package com.erdees.foodcostcalc.data.searchengine

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class SearchEngineRepository {

  private val searchWordLive = MutableLiveData<String>()

  fun searchFor(word: String) {
    searchWordLive.value = word
  }

  fun getWhatToSearchFor() = searchWordLive as LiveData<String>

  companion object {
    @Volatile
    private var instance: SearchEngineRepository? = null

    fun getInstance() =
      instance ?: synchronized(this) {
        instance
          ?: SearchEngineRepository().also { instance = it }
      }
  }
}
