package com.erdees.foodcostcalc.ui.screens.dishes.forms.existingcomponent

import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.Preferences
import com.erdees.foodcostcalc.domain.model.product.ProductDomain
import com.erdees.foodcostcalc.utils.UnitsUtils
import com.erdees.foodcostcalc.utils.Utils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ExistingProductFormViewModel : ViewModel(), KoinComponent {

    private val preferences: Preferences by inject()

    // To control the dropdown for "unit for dish"
    val unitForDishDropdownExpanded = MutableStateFlow(false)

    private val _formData = MutableStateFlow(ExistingProductFormData())
    val formData: StateFlow<ExistingProductFormData> = _formData

    // Enable button if quantity is a valid number and a unit is selected
    val isAddButtonEnabled = formData.map {
        it.quantityForDish.toDoubleOrNull() != null && it.unitForDish.isNotEmpty()
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    fun updateFormData(newValue: ExistingProductFormData) {
        _formData.update { newValue }
    }

    // This will hold the units compatible with the selected product's base unit
    val compatibleUnitsForDish = MutableStateFlow<Set<String>>(emptySet())

    /**
     * Populates the compatible units for the dish based on the selected product's primary unit.
     * This should be called when the form is initialized with a selected product.
     */
    fun setProductContext(product: ProductDomain, resources: Resources) {
        viewModelScope.launch {
            // Determine the unit type from the product's own unit (e.g., if product is in "kg", show "g", "kg", "oz", "lb")
            // This assumes ProductDomain has a field like 'unit' or 'purchaseUnit'
            val productBaseUnit = product.unit // Or product.purchaseUnit, product.mainUnit etc.
            val unitType = UnitsUtils.getUnitType(productBaseUnit)

            if (unitType != null) {
                val metricUsed = preferences.metricUsed.first()
                val imperialUsed = preferences.imperialUsed.first()
                val units = Utils.generateUnitSet(unitType, metricUsed, imperialUsed)
                compatibleUnitsForDish.value = units

                // Set a default unit if current selection is empty and units are available
                if (_formData.value.unitForDish.isEmpty() && units.isNotEmpty()) {
                    _formData.update { it.copy(unitForDish = units.first()) }
                }
            } else {
                // Handle case where product base unit is unknown or not supported
                // Maybe default to a generic set of units or leave it empty
                compatibleUnitsForDish.value = Utils.getUnitsSet(resources, preferences.metricUsed.first(), preferences.imperialUsed.first()) // Fallback to all units
                if (_formData.value.unitForDish.isEmpty() && compatibleUnitsForDish.value.isNotEmpty()){
                    _formData.update { it.copy(unitForDish = compatibleUnitsForDish.value.first()) }
                }
            }
        }
    }


    /**
     * Call this when the "Add Ingredient" button is clicked.
     * It should probably return the form data to the calling screen/ViewModel.
     */
    fun onAddIngredientClick() {
        _formData.value = ExistingProductFormData()
        unitForDishDropdownExpanded.value = false
    }
}