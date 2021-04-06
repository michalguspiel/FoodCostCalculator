package com.erdees.foodcostcalc.viewmodel.adaptersViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.erdees.foodcostcalc.data.basic.BasicDataBase
import com.erdees.foodcostcalc.data.basic.BasicRepository

class RecyclerViewAdapterViewModel(application: Application) : AndroidViewModel(application) {
    val basicRepository: BasicRepository

    init {
        val basicDao = BasicDataBase.getInstance().basicDao
        basicRepository = BasicRepository(basicDao)
    }

    fun setOpenAddFlag(boolean: Boolean) = basicRepository.setOpenAddFlag(boolean)
}