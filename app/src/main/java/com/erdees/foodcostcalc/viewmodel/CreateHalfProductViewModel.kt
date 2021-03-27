package com.erdees.foodcostcalc.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.AppRoomDataBase
import com.erdees.foodcostcalc.data.halfproduct.HalfProductRepository
import com.erdees.foodcostcalc.model.HalfProduct
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CreateHalfProductViewModel(application: Application): AndroidViewModel(application) {

    private val halfProductRepository: HalfProductRepository
    init {
        val halfProductDao = AppRoomDataBase.getDatabase(application).halfProductDao()
        halfProductRepository = HalfProductRepository(halfProductDao)
    }
    fun addHalfProduct(halfProduct: HalfProduct) {
        viewModelScope.launch(Dispatchers.IO) {
            halfProductRepository.addHalfProduct(halfProduct)
        }
    }
}