package com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.createHalfProductDialogFragment

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.Preferences
import com.erdees.foodcostcalc.data.model.HalfProductBase
import com.erdees.foodcostcalc.data.repository.HalfProductRepository
import com.erdees.foodcostcalc.utils.Constants
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class CreateHalfProductFragmentViewModel : ViewModel(), KoinComponent {

    private val halfProductRepository: HalfProductRepository by inject()
    val preferences : Preferences by inject()
    private val firebaseAnalytics: FirebaseAnalytics by inject()

    private fun sendDataAboutHalfProductCreated(halfProductBase: HalfProductBase) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.VALUE, halfProductBase.name)
        firebaseAnalytics.logEvent(Constants.HALF_PRODUCT_CREATED, bundle)
    }

    fun addHalfProduct(name: String, unit: String) {
        val halfProductBase = HalfProductBase(0, name, unit)
        with(halfProductBase) {
            addHalfProduct(this)
            sendDataAboutHalfProductCreated(this)
        }
    }

    private fun addHalfProduct(halfProductBase: HalfProductBase) {
        viewModelScope.launch(Dispatchers.IO) {
            halfProductRepository.addHalfProduct(halfProductBase)
        }
    }
}
