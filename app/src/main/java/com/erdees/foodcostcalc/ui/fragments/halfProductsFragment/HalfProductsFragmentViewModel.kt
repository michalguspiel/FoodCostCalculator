package com.erdees.foodcostcalc.ui.fragments.halfProductsFragment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.erdees.foodcostcalc.data.AppRoomDataBase
import com.erdees.foodcostcalc.data.basic.BasicDataBase
import com.erdees.foodcostcalc.data.basic.BasicRepository
import com.erdees.foodcostcalc.data.halfProductWithProductsIncluded.HalfProductWithProductsIncludedRepository

class HalfProductsFragmentViewModel(application: Application) : AndroidViewModel(application) {

    val halfProductWithProductsIncludedRepository: HalfProductWithProductsIncludedRepository
    val basicRepository: BasicRepository

    init {
        val basicDao = BasicDataBase.getInstance().basicDao
        basicRepository = BasicRepository(basicDao)

        val halfProductWithProductsIncludedDao =
            AppRoomDataBase.getDatabase(application).halfProductWithProductsIncludedDao()
        halfProductWithProductsIncludedRepository =
            HalfProductWithProductsIncludedRepository(halfProductWithProductsIncludedDao)
    }


    fun getHalfProductWithProductIncluded() = halfProductWithProductsIncludedRepository.readAllData

    fun getWhatToSearchFor() = basicRepository.getWhatToSearchFor()
}
