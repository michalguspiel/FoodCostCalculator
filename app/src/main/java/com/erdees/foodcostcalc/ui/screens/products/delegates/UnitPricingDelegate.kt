package com.erdees.foodcostcalc.ui.screens.products.delegates

import com.erdees.foodcostcalc.domain.model.units.MeasurementUnit
import com.erdees.foodcostcalc.utils.onNumericValueChange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Delegate for unit pricing logic:
 * - Unit price and unit management
 * - Unit-specific validation
 */
class UnitPricingDelegate(
    private val scope: CoroutineScope
) {

    private val _unitPrice = MutableStateFlow("")
    val unitPrice: StateFlow<String> = _unitPrice

    private val _unitPriceUnit = MutableStateFlow(MeasurementUnit.KILOGRAM)
    val unitPriceUnit: StateFlow<MeasurementUnit> = _unitPriceUnit

    fun updateUnitPrice(newPrice: String) {
        _unitPrice.value = onNumericValueChange(_unitPrice.value, newPrice)
    }

    fun updateUnitPriceUnit(newUnit: MeasurementUnit) {
        _unitPriceUnit.value = newUnit
    }

    fun reset() {
        _unitPrice.value = ""
        _unitPriceUnit.value = MeasurementUnit.KILOGRAM
    }

    /**
     * Validation for unit pricing fields
     */
    fun createValidation(): StateFlow<Boolean> {
        return unitPrice.map { price ->
            price.toDoubleOrNull() != null
        }.stateIn(scope, SharingStarted.Lazily, false)
    }
}
