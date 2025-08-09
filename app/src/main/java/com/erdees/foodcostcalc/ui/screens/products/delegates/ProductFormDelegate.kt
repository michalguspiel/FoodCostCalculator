package com.erdees.foodcostcalc.ui.screens.products.delegates

import com.erdees.foodcostcalc.data.Preferences
import com.erdees.foodcostcalc.domain.model.product.InputMethod
import com.erdees.foodcostcalc.domain.model.units.MeasurementUnit
import com.erdees.foodcostcalc.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Core product form delegate handling common logic:
 * - Product name management
 * - Price mode (package/unit) toggling
 * - Tax field management
 * - Waste field management
 * - Units loading
 */
class ProductFormDelegate(
    private val preferences: Preferences,
    private val scope: CoroutineScope
) {
    
    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name
    
    private val _tax = MutableStateFlow("")
    val tax: StateFlow<String> = _tax
    
    private val _waste = MutableStateFlow("")
    val waste: StateFlow<String> = _waste
    
    private val _inputMethod = MutableStateFlow(InputMethod.PACKAGE)
    val inputMethod: StateFlow<InputMethod> = _inputMethod
    
    private val _units = MutableStateFlow<Set<MeasurementUnit>>(setOf())
    val units: StateFlow<Set<MeasurementUnit>> = _units
    
    val showTaxField: StateFlow<Boolean> = preferences.showProductTax
        .stateIn(scope, SharingStarted.Eagerly, true)
    
    init {
        loadUnits()
        
        // Initialize tax field based on preference
        scope.launch {
            showTaxField.collect { show ->
                if (!show && _tax.value.isEmpty()) {
                    _tax.value = "0.0"
                }
            }
        }
    }
    
    fun updateName(newName: String) {
        _name.value = newName
    }
    
    fun updateTax(newTax: String) {
        _tax.value = newTax
    }
    
    fun updateWaste(newWaste: String) {
        _waste.value = newWaste
    }
    
    fun toggleInputMethod() {
        _inputMethod.value = when (_inputMethod.value) {
            InputMethod.PACKAGE -> InputMethod.UNIT
            InputMethod.UNIT -> InputMethod.PACKAGE
        }
    }
    
    fun setInputMethod(method: InputMethod) {
        _inputMethod.value = method
    }
    
    fun reset() {
        _name.value = ""
        _tax.value = if (!showTaxField.value) "0.0" else ""
        _waste.value = ""
        _inputMethod.value = InputMethod.PACKAGE
    }
    
    private fun loadUnits() {
        scope.launch {
            val metricUsed = preferences.metricUsed.first()
            val imperialUsed = preferences.imperialUsed.first()
            _units.value = Utils.getCompleteUnitsSet(metricUsed, imperialUsed)
        }
    }
    
    /**
     * Creates a base validation state that can be combined with specific pricing validation
     */
    fun createBaseValidation(): StateFlow<Boolean> {
        return combine(
            name,
            tax,
            showTaxField
        ) { nameValue, taxValue, showTax ->
            nameValue.isNotBlank() && (!showTax || taxValue.toDoubleOrNull() != null)
        }.stateIn(scope, SharingStarted.Lazily, false)
    }
}
