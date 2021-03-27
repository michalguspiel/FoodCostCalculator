package com.erdees.foodcostcalc.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.AppRoomDataBase
import com.erdees.foodcostcalc.data.basic.BasicDataBase
import com.erdees.foodcostcalc.data.basic.BasicRepository
import com.erdees.foodcostcalc.data.grandDish.GrandDishRepository
import com.erdees.foodcostcalc.data.halfproduct.HalfProductRepository
import com.erdees.foodcostcalc.data.halfProductWithProductsIncluded.HalfProductWithProductsIncludedRepository
import com.erdees.foodcostcalc.data.productIncludedInHalfProduct.ProductIncludedInHalfProductRepository
import com.erdees.foodcostcalc.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HalfProductsViewModel(application: Application) : AndroidViewModel(application) {

    val halfProductWithProductsIncludedRepository : HalfProductWithProductsIncludedRepository
    val basicRepository : BasicRepository

    init {
        val basicDao = BasicDataBase.getInstance().basicDao
        basicRepository = BasicRepository(basicDao)

        val halfProductWithProductsIncludedDao = AppRoomDataBase.getDatabase(application).halfProductWithProductsIncludedDao()
        halfProductWithProductsIncludedRepository = HalfProductWithProductsIncludedRepository(halfProductWithProductsIncludedDao)
    }


    fun getHalfProductWithProductIncluded() = halfProductWithProductsIncludedRepository.readAllData

    fun getWhatToSearchFor() = basicRepository.getWhatToSearchFor()
}
