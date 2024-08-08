package com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.createHalfProductDialogFragment

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.Preferences
import com.erdees.foodcostcalc.data.model.HalfProduct
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

    private fun sendDataAboutHalfProductCreated(halfProduct: HalfProduct) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.VALUE, halfProduct.name)
        firebaseAnalytics.logEvent(Constants.HALF_PRODUCT_CREATED, bundle)
    }

    fun addHalfProduct(name: String, unit: String) {
        val halfProduct = HalfProduct(0, name, unit)
        with(halfProduct) {
            addHalfProduct(this)
            sendDataAboutHalfProductCreated(this)
        }
    }

    private fun addHalfProduct(halfProduct: HalfProduct) {
        viewModelScope.launch(Dispatchers.IO) {
            halfProductRepository.addHalfProduct(halfProduct)
        }
    }
}
