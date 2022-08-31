package com.erdees.foodcostcalc.data.basic

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class BasicDao {

  private var searchWord: String = ""
  private val searchWordLive = MutableLiveData<String>()

  init {
    searchWordLive.value = searchWord
  }

  fun searchFor(word: String) {
    searchWord = word
    searchWordLive.value = searchWord
  }

  fun getWhatToSearchFor() = searchWordLive as LiveData<String>
}
