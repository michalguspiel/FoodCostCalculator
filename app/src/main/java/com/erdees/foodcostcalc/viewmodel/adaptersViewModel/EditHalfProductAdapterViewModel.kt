package com.erdees.foodcostcalc.viewmodel.adaptersViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.model.HalfProductBase
import com.erdees.foodcostcalc.data.repository.HalfProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class EditHalfProductAdapterViewModel : ViewModel(), KoinComponent {
  private val halfProductRepository: HalfProductRepository by inject()

  fun editHalfProducts(halfProductBase: HalfProductBase) {
    viewModelScope.launch(Dispatchers.IO) {
      halfProductRepository.editHalfProduct(halfProductBase)
    }
  }
}
