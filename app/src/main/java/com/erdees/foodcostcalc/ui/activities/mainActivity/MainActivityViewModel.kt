package com.erdees.foodcostcalc.ui.activities.mainActivity

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.erdees.foodcostcalc.data.basic.BasicDataBase
import com.erdees.foodcostcalc.data.basic.BasicRepository

class MainActivityViewModel(application: Application):AndroidViewModel(application) {

    val basicRepository : BasicRepository

    init {
        val basicDao = BasicDataBase.getInstance().basicDao
        basicRepository = BasicRepository(basicDao)
    }

    fun observeOpenAddFlag() = basicRepository.observeOpenAddFlag()

    fun observeOpenCreateDishFlag() = basicRepository.observeOpenCreateDishFlag()

    fun observeOpenCreateHalfProductFlag() = basicRepository.observeOpenCreateHalfProductFlag()

    fun searchFor(word: String) {
        basicRepository.searchFor(word)
    }
}