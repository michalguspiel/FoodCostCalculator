package com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.createHalfProductDialogFragment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.AppRoomDataBase
import com.erdees.foodcostcalc.data.halfproduct.HalfProductRepository
import com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.models.HalfProductModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CreateHalfProductFragmentViewModel(application: Application) : AndroidViewModel(application) {

    private val halfProductRepository: HalfProductRepository

    init {
        val halfProductDao = AppRoomDataBase.getDatabase(application).halfProductDao()
        halfProductRepository = HalfProductRepository(halfProductDao)
    }

    fun addHalfProduct(halfProductModel: HalfProductModel) {
        viewModelScope.launch(Dispatchers.IO) {
            halfProductRepository.addHalfProduct(halfProductModel)
        }
    }
}