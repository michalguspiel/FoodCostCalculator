package com.erdees.foodcostcalc.ui.fragments.halfProductsFragment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.erdees.foodcostcalc.data.AppRoomDataBase
import com.erdees.foodcostcalc.data.searchengine.SearchEngineRepository
import com.erdees.foodcostcalc.data.halfProductWithProductsIncluded.HalfProductWithProductsIncludedRepository

/**TODO REFACTORING INTO VIEW BINDING + MVVM PATTERN IMPROVEMENT */


class HalfProductsFragmentViewModel(application: Application) : AndroidViewModel(application) {

    private val halfProductWithProductsIncludedRepository: HalfProductWithProductsIncludedRepository
    private val searchEngineRepository: SearchEngineRepository = SearchEngineRepository.getInstance()

  init {

    val halfProductWithProductsIncludedDao =
            AppRoomDataBase.getDatabase(application).halfProductWithProductsIncludedDao()
        halfProductWithProductsIncludedRepository =
            HalfProductWithProductsIncludedRepository(halfProductWithProductsIncludedDao)
    }


    fun getHalfProductWithProductIncluded() = halfProductWithProductsIncludedRepository.readAllData

    fun getWhatToSearchFor() = searchEngineRepository.getWhatToSearchFor()
}
