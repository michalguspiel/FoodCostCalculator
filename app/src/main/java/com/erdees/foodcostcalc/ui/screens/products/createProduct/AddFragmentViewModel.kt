package com.erdees.foodcostcalc.ui.screens.products.createProduct

import android.content.res.Resources
import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.Preferences
import com.erdees.foodcostcalc.data.model.ProductBase
import com.erdees.foodcostcalc.data.repository.ProductRepository
import com.erdees.foodcostcalc.utils.Constants
import com.erdees.foodcostcalc.utils.Utils
import com.erdees.foodcostcalc.utils.Utils.formatResultAndCheckCommas
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AddFragmentViewModel : ViewModel(),
  KoinComponent {

  private val productRepository: ProductRepository by inject()
  private val preferences: Preferences by inject()
  lateinit var firebaseAnalytics: FirebaseAnalytics

  var chosenUnit: String = ""
  var unitList: MutableList<String> = mutableListOf()

  fun getUnits(resources: Resources) {
    unitList = Utils.getUnits(resources, preferences)
  }

  fun addProduct(
    productName: String,
    productPrice: Double,
    productTax: Double,
    productWaste: Double,
    chosenUnit: String
  ) {
    val product = ProductBase(
      0,
      productName, productPrice, productTax, productWaste, chosenUnit
    )
    addProduct(product)
    sendDataAboutProduct(product)
  }

  private fun addProduct(product: ProductBase) {
    viewModelScope.launch(Dispatchers.IO) {
      productRepository.addProduct(product)
    }
  }

  private fun sendDataAboutProduct(product: ProductBase) {
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