package com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.editHalfProductDialogFragment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.erdees.foodcostcalc.data.AppRoomDataBase
import com.erdees.foodcostcalc.data.halfproduct.HalfProductRepository

class EditHalfProductFragmentViewModel(application: Application) : AndroidViewModel(application) {

  private val halfProductDao = AppRoomDataBase.getDatabase(application).halfProductDao()
  private val halfProductRepository: HalfProductRepository =
    HalfProductRepository.getInstance(halfProductDao)
}
