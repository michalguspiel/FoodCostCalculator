package com.erdees.foodcostcalc.ui.screens.products.delegates

import com.erdees.foodcostcalc.domain.model.units.MeasurementUnit
import com.erdees.foodcostcalc.utils.onNumericValueChange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * Delegate for package pricing logic:
 * - Package price, quantity, unit management
 * - Canonical price calculation
 * - Package-specific validation
 */
class PackagePricingDelegate(
    private val scope: CoroutineScope
) {

    private val _packagePrice = MutableStateFlow("")
    val packagePrice: StateFlow<String> = _packagePrice

    private val _packageQuantity = MutableStateFlow("")
    val packageQuantity: StateFlow<String> = _packageQuantity

    private val _packageUnit = MutableStateFlow(MeasurementUnit.KILOGRAM)
    val packageUnit: StateFlow<MeasurementUnit> = _packageUnit

    // Computed canonical price and unit
    val canonicalPriceAndUnit: StateFlow<Pair<Double?, MeasurementUnit?>> = combine(
        packagePrice,
        packageQuantity,
        packageUnit
    ) { price, quantity, unit ->
        val priceValue = price.toDoubleOrNull()
        val quantityValue = quantity.toDoubleOrNull()

        if (priceValue != null && quantityValue != null && quantityValue > 0) {
            try {
                val (canonicalPrice, canonicalUnit) = unit.calculateCanonicalPrice(
                    packagePrice = priceValue,
                    packageQuantity = quantityValue
                )
                canonicalPrice to canonicalUnit
            } catch (_: Exception) {
                null to null
            }
        } else {
            null to null
        }
    }.stateIn(scope, SharingStarted.Lazily, null to null)

    fun updatePackagePrice(newPrice: String) {
        _packagePrice.value = onNumericValueChange(_packagePrice.value, newPrice)
    }

    fun updatePackageQuantity(newQuantity: String) {
        _packageQuantity.value = onNumericValueChange(_packageQuantity.value, newQuantity)
    }

    fun updatePackageUnit(newUnit: MeasurementUnit) {
        _packageUnit.value = newUnit
    }

    fun reset() {
        _packagePrice.value = ""
        _packageQuantity.value = ""
        _packageUnit.value = MeasurementUnit.KILOGRAM
    }

    /**
     * Validation for package pricing fields
     */
    fun createValidation(): StateFlow<Boolean> {
        return combine(
            packagePrice,
            packageQuantity,
            packageUnit
        ) { price, quantity, unit ->
            val priceValue = price.toDoubleOrNull()
            val quantityValue = quantity.toDoubleOrNull()

            priceValue != null && quantityValue != null && quantityValue > 0
        }.stateIn(scope, SharingStarted.Lazily, false)
    }
}
