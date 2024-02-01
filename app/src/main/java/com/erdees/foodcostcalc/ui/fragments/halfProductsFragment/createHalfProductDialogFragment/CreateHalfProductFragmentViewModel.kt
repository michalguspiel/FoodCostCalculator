package com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.createHalfProductDialogFragment

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.AppRoomDataBase
import com.erdees.foodcostcalc.data.halfproduct.HalfProductRepository
import com.erdees.foodcostcalc.entities.HalfProduct
import com.erdees.foodcostcalc.ui.fragments.settingsFragment.SharedPreferences
import com.erdees.foodcostcalc.utils.Constants
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CreateHalfProductFragmentViewModel(application: Application) : AndroidViewModel(application) {

    private val halfProductRepository: HalfProductRepository

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    init {
        val halfProductDao = AppRoomDataBase.getDatabase(application).halfProductDao()
        halfProductRepository = HalfProductRepository.getInstance(halfProductDao)
    }

    val sharedPreferences = SharedPreferences(application)

    fun updateFirebase() {
        firebaseAnalytics = FirebaseAnalytics.getInstance(getApplication())
    }

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
