package com.erdees.foodcostcalc.ui.fragments.addFragment

import android.app.Application
import android.content.res.Resources
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.AppRoomDataBase
import com.erdees.foodcostcalc.data.product.ProductRepository
import com.erdees.foodcostcalc.entities.Product
import com.erdees.foodcostcalc.utils.Constants
import com.erdees.foodcostcalc.utils.Utils
import com.erdees.foodcostcalc.utils.Utils.formatResultAndCheckCommas
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddFragmentViewModel(application: Application) : AndroidViewModel(application) {

    private val productRepository: ProductRepository

    lateinit var firebaseAnalytics: FirebaseAnalytics

    var chosenUnit: String = ""
    var unitList: MutableList<String> = mutableListOf()

    init {
        val productDao = AppRoomDataBase.getDatabase(application).productDao()
        productRepository = ProductRepository.getInstance(productDao)
    }

    fun getUnits(
        resources: Resources,
        sharedPreferences: com.erdees.foodcostcalc.ui.fragments.settingsFragment.SharedPreferences
    ) {
        unitList = Utils.getUnits(resources, sharedPreferences)
    }

    fun addProduct(
        productName: String,
        productPrice: Double,
        productTax: Double,
        productWaste: Double,
        chosenUnit : String
    ) {
        val product = Product(
            0,
            productName, productPrice, productTax, productWaste, chosenUnit
        )
        addProducts(product)
        sendDataAboutProduct(product)
    }

    private fun addProducts(product: Product) {
        viewModelScope.launch(Dispatchers.IO) {
            productRepository.addProduct(product)
        }
    }

    private fun sendDataAboutProduct(product: Product) {
        val bundle = Bundle()
        bundle.putString(Constants.PRODUCT_NAME, product.name)
        bundle.putString(Constants.PRODUCT_TAX, product.tax.toString())
        bundle.putString(Constants.PRODUCT_WASTE, product.waste.toString())
        bundle.putString(Constants.PRODUCT_UNIT, product.unit)
        bundle.putString(Constants.PRODUCT_PRICE_PER_UNIT, product.pricePerUnit.toString())
        firebaseAnalytics.logEvent(Constants.PRODUCT_CREATED, bundle)
    }

    fun calculateWaste(calcWeight: Double, calcWaste: Double): String {
        val result = (100 * calcWaste) / calcWeight
        return formatResultAndCheckCommas(result)
    }

    fun calculatePricePerPiece(pricePerBox: Double, quantityInBox: Double): String {
        val result = pricePerBox / quantityInBox
        return formatResultAndCheckCommas(result)
    }
}
