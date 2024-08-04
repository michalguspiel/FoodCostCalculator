package com.erdees.foodcostcalc.viewmodel.adaptersViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.AppRoomDataBase
import com.erdees.foodcostcalc.data.halfproduct.HalfProductRepository
import com.erdees.foodcostcalc.entities.HalfProduct
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditHalfProductAdapterViewModel(application: Application):AndroidViewModel(application) {

  private val halfProductDao = AppRoomDataBase.getDatabase(application).halfProductDao()

  private val halfProductRepository = HalfProductRepository.getInstance(halfProductDao)

  fun editHalfProducts(halfProduct: HalfProduct) {
        viewModelScope.launch(Dispatchers.IO) {
            halfProductRepository.editHalfProduct(halfProduct)
        }
    }
}
