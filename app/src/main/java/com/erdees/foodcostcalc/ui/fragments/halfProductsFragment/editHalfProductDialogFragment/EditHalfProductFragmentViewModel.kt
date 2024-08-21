package com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.editHalfProductDialogFragment

import androidx.lifecycle.ViewModel
import com.erdees.foodcostcalc.data.repository.HalfProductRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class EditHalfProductFragmentViewModel : ViewModel(), KoinComponent {

  private val halfProductRepository: HalfProductRepository by inject()
}
