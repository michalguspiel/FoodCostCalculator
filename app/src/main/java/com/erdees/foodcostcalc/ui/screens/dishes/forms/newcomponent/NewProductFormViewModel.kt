package com.erdees.foodcostcalc.ui.screens.dishes.forms.newcomponent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.Preferences
import com.erdees.foodcostcalc.domain.model.product.InputMethod
import com.erdees.foodcostcalc.domain.model.units.MeasurementUnit
import com.erdees.foodcostcalc.domain.model.units.UnitCategory
import com.erdees.foodcostcalc.utils.UnitsUtils
import com.erdees.foodcostcalc.utils.Utils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class NewProductFormViewModel : ViewModel(), KoinComponent {

    private val preferences: Preferences by inject()

    // Core state
    private val _currentStep = MutableStateFlow(NewProductWizardStep.DEFINE_PURCHASE)
    val currentStep: StateFlow<NewProductWizardStep> = _currentStep

    private val _formData = MutableStateFlow(NewProductFormData())
    val formData: StateFlow<NewProductFormData> = _formData

    // Dropdown states
    private val _productCreationDropdownExpanded = MutableStateFlow(false)
    val productCreationDropdownExpanded: StateFlow<Boolean> = _productCreationDropdownExpanded

    private val _productAdditionDropdownExpanded = MutableStateFlow(false)
    val productAdditionDropdownExpanded: StateFlow<Boolean> = _productAdditionDropdownExpanded

    // Units
    private val _productCreationUnits = MutableStateFlow<Set<MeasurementUnit>>(setOf())
    val productCreationUnits: StateFlow<Set<MeasurementUnit>> = _productCreationUnits

    val productAdditionUnits = formData
        .distinctUntilChanged { old, new ->
            old.purchaseUnit == new.purchaseUnit
        }.map { formData ->
            val unitType = UnitsUtils.getUnitType(formData.purchaseUnit)
            unitType?.let { getUnitList(it) } ?: emptySet()
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    private fun isPackagePriceValid(data: NewProductFormData): Boolean {
        return data.packagePrice.toDoubleOrNull() != null &&
                data.packageQuantity.toDoubleOrNull() != null &&
                data.packageQuantity.toDouble() > 0 &&
                data.packageUnit != null
    }

    private fun isUnitPriceValid(data: NewProductFormData): Boolean {
        return data.unitPrice.toDoubleOrNull() != null &&
                data.unitPriceUnit != null
    }

    val isNextButtonEnabled = combine(
        formData,
        _currentStep
    ) { data, step ->
        when (step) {
            NewProductWizardStep.DEFINE_PURCHASE -> {
                val hasValidPrice = when (data.inputMethod) {
                    InputMethod.PACKAGE -> isPackagePriceValid(data)
                    InputMethod.UNIT -> isUnitPriceValid(data)
                }
                hasValidPrice
            }

            NewProductWizardStep.DEFINE_USAGE -> false
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    val isCreateButtonEnabled = combine(
        formData,
        _currentStep
    ) { data, step ->
        when (step) {
            NewProductWizardStep.DEFINE_PURCHASE -> false // Not applicable for step 1
            NewProductWizardStep.DEFINE_USAGE -> {
                val hasValidStep1 = when (data.inputMethod) {
                    InputMethod.PACKAGE -> isPackagePriceValid(data)
                    InputMethod.UNIT -> isUnitPriceValid(data)
                }
                val hasValidStep2 = data.quantityAddedToDish.toDoubleOrNull() != null &&
                        data.quantityAddedToDishUnit != null

                hasValidStep1 && hasValidStep2
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    init {
        viewModelScope.launch {
            productAdditionUnits
                .filter { it.isNotEmpty() }
                .collect { availableUnits ->
                    if (formData.value.quantityAddedToDishUnit == null) {
                        _formData.update { it.copy(quantityAddedToDishUnit = availableUnits.first()) }
                    }
                }
        }
    }

    fun goToNextStep() {
        when (_currentStep.value) {
            NewProductWizardStep.DEFINE_PURCHASE -> {
                _currentStep.value = NewProductWizardStep.DEFINE_USAGE

                viewModelScope.launch {
                    val currentFormData = formData.value
                    val purchaseUnit = currentFormData.purchaseUnit
                    val currentDishUnit = currentFormData.quantityAddedToDishUnit

                    if (purchaseUnit != null &&
                        (currentDishUnit == null || currentDishUnit.category != purchaseUnit.category)) {

                        val availableUnits = getUnitList(purchaseUnit.category)
                        val newDishUnit = availableUnits.firstOrNull()

                        if (newDishUnit != null) {
                            _formData.update {
                                it.copy(quantityAddedToDishUnit = newDishUnit)
                            }
                        }
                    }
                }
            }

            NewProductWizardStep.DEFINE_USAGE -> {
                // Already at last step
            }
        }
    }

    fun goToPreviousStep() {
        when (_currentStep.value) {
            NewProductWizardStep.DEFINE_PURCHASE -> {
                // Already at first step
            }

            NewProductWizardStep.DEFINE_USAGE -> {
                _currentStep.value = NewProductWizardStep.DEFINE_PURCHASE
            }
        }
    }

    fun resetToFirstStep() {
        _currentStep.value = NewProductWizardStep.DEFINE_PURCHASE
    }

    fun updateFormData(newValue: NewProductFormData) {
        _formData.update { newValue }
    }

    fun setProductCreationDropdownExpanded(expanded: Boolean) {
        _productCreationDropdownExpanded.value = expanded
    }

    fun setProductAdditionDropdownExpanded(expanded: Boolean) {
        _productAdditionDropdownExpanded.value = expanded
    }

    // Existing functions
    fun getProductCreationUnits() {
        viewModelScope.launch {
            val metricUsed = preferences.metricUsed.first()
            val imperialUsed = preferences.imperialUsed.first()
            _productCreationUnits.update { Utils.getUnitsSet(metricUsed, imperialUsed) }
        }
    }

    private suspend fun getUnitList(unitType: UnitCategory): Set<MeasurementUnit> {
        val metricUnits = preferences.metricUsed.first()
        val imperialUnits = preferences.imperialUsed.first()
        return Utils.generateUnitSet(
            unitType,
            metricUnits,
            imperialUnits
        )
    }

    fun onAddIngredientClick() {
        _formData.value = NewProductFormData()
        _currentStep.value = NewProductWizardStep.DEFINE_PURCHASE
    }
}