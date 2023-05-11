package com.erdees.foodcostcalc.ui.activities.mainActivity

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.erdees.foodcostcalc.data.searchengine.SearchEngineRepository

class MainActivityViewModel(application: Application):AndroidViewModel(application) {

    private val searchEngineRepository = SearchEngineRepository.getInstance()
    fun searchFor(word: String) {
        searchEngineRepository.searchFor(word)
    }
}
