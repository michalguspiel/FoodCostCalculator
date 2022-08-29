package com.erdees.foodcostcalc.data.basic

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class BasicDao {
  /**Flag provides an information if product/dishModel was just deleted and if fragment should close itself */
  private var mutableFlag: Boolean = true
  private val flag = MutableLiveData<Boolean>()

  private var searchWord: String = ""
  private val searchWordLive = MutableLiveData<String>()

  init {
    flag.value = mutableFlag
    searchWordLive.value = searchWord
  }

  fun searchFor(word: String) {
    searchWord = word
    searchWordLive.value = searchWord
  }

  fun getWhatToSearchFor() = searchWordLive as LiveData<String>

  fun setFlag(boolean: Boolean) {
    mutableFlag = boolean
    flag.value = mutableFlag
  }

  fun getFlag() = flag as LiveData<Boolean>
}
