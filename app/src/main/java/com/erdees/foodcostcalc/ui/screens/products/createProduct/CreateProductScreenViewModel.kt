package com.erdees.foodcostcalc.ui.screens.products.createProduct

import android.content.res.Resources
import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.Preferences
import com.erdees.foodcostcalc.data.model.ProductBase
import com.erdees.foodcostcalc.data.repository.AnalyticsRepository
import com.erdees.foodcostcalc.data.repository.ProductRepository
import com.erdees.foodcostcalc.domain.model.InteractionType
import com.erdees.foodcostcalc.domain.model.ScreenState
import com.erdees.foodcostcalc.utils.Constants
import com.erdees.foodcostcalc.utils.Utils
import com.erdees.foodcostcalc.utils.Utils.formatResultAndCheckCommas
import com.erdees.foodcostcalc.utils.onNumericValueChange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class CreateProductScreenViewModel : ViewModel(), KoinComponent {

    private val productRepository: ProductRepository by inject()
    private val preferences: Preferences by inject()
    private val analyticsRepository: AnalyticsRepository by inject()

    private val metricUsed = preferences.metricUsed.stateIn(viewModelScope, SharingStarted.Lazily, true)
    private val imperialUsed = preferences.imperialUsed.stateIn(viewModelScope, SharingStarted.Lazily, true)

    private val _screenState = MutableStateFlow<ScreenState>(ScreenState.Idle)
    val screenState: StateFlow<ScreenState> get() = _screenState

    fun resetScreenState() {
        _screenState.value = ScreenState.Idle
    }

    private val _productName = MutableStateFlow("")
    val productName: StateFlow<String> get() = _productName

    fun updateProductName(name: String) {
        _productName.value = name
    }

    private val _productPrice = MutableStateFlow("")
    val productPrice: StateFlow<String> get() = _productPrice

    fun updateProductPrice(price: String) {
        onNumericValueChange(price, _productPrice)
    }

    private val _productTax = MutableStateFlow("")
    val productTax: StateFlow<String> get() = _productTax

    fun updateProductTax(tax: String) {
        onNumericValueChange(tax, _productTax)
    }

    private val _productWaste = MutableStateFlow("")
    val productWaste: StateFlow<String> get() = _productWaste

    fun updateProductWaste(waste: String) {
        onNumericValueChange(waste, _productWaste)
    }

    val units = MutableStateFlow<Set<String>>(setOf())

    fun getUnits(resources: Resources) {
        units.update { Utils.getUnitsSet(resources, metricUsed.value, imperialUsed.value) }
    }

    private var _selectedUnit = MutableStateFlow("")
    val selectedUnit: StateFlow<String> = _selectedUnit

    fun selectUnit(unit: String) {
        _selectedUnit.value = unit
    }

    val addButtonEnabled: StateFlow<Boolean> = combine(
        productName,
        productPrice,
        productTax,
        productWaste,
        selectedUnit
    ) { name, price, tax, waste, unit ->
        name.isNotBlank() &&
                unit.isNotBlank() &&
                price.toDoubleOrNull() != null &&
                tax.toDoubleOrNull() != null &&
                waste.toDoubleOrNull() != null
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    val countPiecePriceEnabled: StateFlow<Boolean> = selectedUnit.map {
        it == "per piece"
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    fun addProduct() {

        val price = productPrice.value.toDoubleOrNull() ?: return
        val tax = productTax.value.toDoubleOrNull() ?: return
        val waste = productWaste.value.toDoubleOrNull() ?: return

        val product = ProductBase(
            0,
            productName.value,
            price,
            tax,
            waste,
            selectedUnit.value
        )
        addProduct(product)
        sendDataAboutProduct(product)
    }

    private fun addProduct(product: ProductBase) {
        _screenState.value = ScreenState.Loading()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                productRepository.addProduct(product)
                _screenState.value = ScreenState.Success()
            } catch (e: Exception) {
                _screenState.value = ScreenState.Error(Error(e.message))
            }
        }
    }

    private fun sendDataAboutProduct(product: ProductBase) {
        val bundle = Bundle()
        bundle.putString(Constants.Analytics.PRODUCT_NAME, product.name)
        bundle.putString(Constants.Analytics.PRODUCT_TAX, product.tax.toString())
        bundle.putString(Constants.Analytics.PRODUCT_WASTE, product.waste.toString())
        bundle.putString(Constants.Analytics.PRODUCT_UNIT, product.unit)
        bundle.putString(
            Constants.Analytics.PRODUCT_PRICE_PER_UNIT,
            product.pricePerUnit.toString()
        )
        analyticsRepository.logEvent(Constants.Analytics.PRODUCT_CREATED, bundle)
    }

    fun onCalculateWaste() {
        _screenState.value = ScreenState.Interaction(InteractionType.CalculateWaste)
    }

    fun onCalculatePiecePrice() {
        _screenState.value = ScreenState.Interaction(InteractionType.CalculatePiecePrice)
    }

    fun calculateWaste(totalQuantity: Double?, wasteQuantity: Double?) {
        totalQuantity ?: return
        wasteQuantity ?: return
        val result = (100 * wasteQuantity) / totalQuantity
        _productWaste.value = formatResultAndCheckCommas(result)
        resetScreenState()
    }

    fun calculatePricePerPiece(boxPrice: Double?, quantityInBox: Int?) {
        boxPrice ?: return
        quantityInBox ?: return
        val result = boxPrice / quantityInBox
        _productPrice.value = formatResultAndCheckCommas(result)
        resetScreenState()
    }
}