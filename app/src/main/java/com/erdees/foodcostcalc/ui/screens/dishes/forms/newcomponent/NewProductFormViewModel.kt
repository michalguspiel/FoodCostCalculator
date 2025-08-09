package com.erdees.foodcostcalc.ui.screens.dishes.forms.newcomponent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.Preferences
import com.erdees.foodcostcalc.domain.model.units.MeasurementUnit
import com.erdees.foodcostcalc.domain.model.units.UnitCategory
import com.erdees.foodcostcalc.ui.screens.products.delegates.NewProductFormBridgeDelegate
import com.erdees.foodcostcalc.ui.screens.products.delegates.PackagePricingDelegate
import com.erdees.foodcostcalc.ui.screens.products.delegates.ProductFormDelegate
import com.erdees.foodcostcalc.ui.screens.products.delegates.UnitPricingDelegate
import com.erdees.foodcostcalc.utils.UnitsUtils
import com.erdees.foodcostcalc.utils.Utils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class NewProductFormViewModel : ViewModel(), KoinComponent {

    private val preferences: Preferences by inject()

    private val productFormDelegate = ProductFormDelegate(preferences, viewModelScope)
    private val packagePricingDelegate = PackagePricingDelegate(viewModelScope)
    private val unitPricingDelegate = UnitPricingDelegate(viewModelScope)
    private val bridgeDelegate = NewProductFormBridgeDelegate(
        productFormDelegate,
        packagePricingDelegate,
        unitPricingDelegate,
        viewModelScope
    )

    private val _currentStep = MutableStateFlow(NewProductWizardStep.DEFINE_PURCHASE)
    val currentStep: StateFlow<NewProductWizardStep> = _currentStep

    private val _quantityAddedToDish = MutableStateFlow("")
    private val _quantityAddedToDishUnit = MutableStateFlow<MeasurementUnit?>(null)

    val formData: StateFlow<NewProductFormData> = combine(
        bridgeDelegate.createFormDataState(),
        _quantityAddedToDish,
        _quantityAddedToDishUnit
    ) { baseFormData, quantity, unit ->
        baseFormData.copy(
            quantityAddedToDish = quantity,
            quantityAddedToDishUnit = unit
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, NewProductFormData())

    private val _productCreationDropdownExpanded = MutableStateFlow(false)
    val productCreationDropdownExpanded: StateFlow<Boolean> = _productCreationDropdownExpanded

    private val _productAdditionDropdownExpanded = MutableStateFlow(false)
    val productAdditionDropdownExpanded: StateFlow<Boolean> = _productAdditionDropdownExpanded

    val productCreationUnits: StateFlow<Set<MeasurementUnit>> = productFormDelegate.units

    val productAdditionUnits = formData
        .distinctUntilChanged { old, new ->
            old.purchaseUnit == new.purchaseUnit
        }.map { formData ->
            val unitType = UnitsUtils.getUnitType(formData.purchaseUnit)
            unitType?.let { getUnitList(it) } ?: emptySet()
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    val isNextButtonEnabled: StateFlow<Boolean> = combine(
        currentStep,
        bridgeDelegate.createValidation()
    ) { step, isFormValid ->
        step == NewProductWizardStep.DEFINE_PURCHASE && isFormValid
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    val isCreateButtonEnabled: StateFlow<Boolean> = combine(
        formData,
        currentStep,
        bridgeDelegate.createValidation()
    ) { data, step, isStep1Valid ->
        if (step == NewProductWizardStep.DEFINE_USAGE) {
            val isStep2Valid = data.quantityAddedToDish.toDoubleOrNull() != null &&
                    data.quantityAddedToDishUnit != null
            isStep1Valid && isStep2Valid
        } else {
            false
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    fun goToNextStep() {
        if (currentStep.value == NewProductWizardStep.DEFINE_PURCHASE) {
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
                        _quantityAddedToDishUnit.value = newDishUnit
                    }
                }
            }
        }
    }

    fun goToPreviousStep() {
        if (currentStep.value == NewProductWizardStep.DEFINE_USAGE) {
            _currentStep.value = NewProductWizardStep.DEFINE_PURCHASE
        }
    }

    fun resetToFirstStep() {
        _currentStep.value = NewProductWizardStep.DEFINE_PURCHASE
    }

    fun updateFormData(newValue: NewProductFormData) {
        bridgeDelegate.syncFromFormData(newValue)

        if (_quantityAddedToDish.value != newValue.quantityAddedToDish) {
            _quantityAddedToDish.value = newValue.quantityAddedToDish
        }
        if (_quantityAddedToDishUnit.value != newValue.quantityAddedToDishUnit) {
            _quantityAddedToDishUnit.value = newValue.quantityAddedToDishUnit
        }
    }

    fun setProductCreationDropdownExpanded(expanded: Boolean) {
        _productCreationDropdownExpanded.value = expanded
    }

    fun setProductAdditionDropdownExpanded(expanded: Boolean) {
        _productAdditionDropdownExpanded.value = expanded
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
        productFormDelegate.reset()
        packagePricingDelegate.reset()
        unitPricingDelegate.reset()

        _quantityAddedToDish.value = ""
        _quantityAddedToDishUnit.value = null

        _currentStep.value = NewProductWizardStep.DEFINE_PURCHASE
    }
}