package com.erdees.foodcostcalc.ui.fragments.addFragment

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.AppRoomDataBase
import com.erdees.foodcostcalc.data.product.ProductRepository
import com.erdees.foodcostcalc.ui.fragments.productsFragment.models.ProductModel
import com.erdees.foodcostcalc.utils.Constants
import com.erdees.foodcostcalc.utils.SharedFunctions.formatResultAndCheckCommas
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddFragmentViewModel(application: Application) : AndroidViewModel(application) {

    private val productRepository: ProductRepository

    lateinit var firebaseAnalytics: FirebaseAnalytics


    init {
        val productDao = AppRoomDataBase.getDatabase(application).productDao()
        productRepository = ProductRepository(productDao)

    }

    fun addProduct(
        productName: String,
        productPrice: Double,
        productTax: Double,
        productWaste: Double,
        chosenUnit: String
    ) {
        val product = ProductModel(
            0,
            productName, productPrice, productTax, productWaste, chosenUnit
        )
        addProducts(product)
        sendDataAboutProduct(product)
    }

    private fun addProducts(productModel: ProductModel) {
        viewModelScope.launch(Dispatchers.IO) {
            productRepository.addProduct(productModel)
        }
    }

    private fun sendDataAboutProduct(productModel: ProductModel) {
        val bundle = Bundle()
        bundle.putString(Constants.PRODUCT_NAME, productModel.name)
        bundle.putString(Constants.PRODUCT_TAX, productModel.tax.toString())
        bundle.putString(Constants.PRODUCT_WASTE, productModel.waste.toString())
        bundle.putString(Constants.PRODUCT_UNIT, productModel.unit)
        bundle.putString(Constants.PRODUCT_PRICE_PER_UNIT, productModel.pricePerUnit.toString())
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