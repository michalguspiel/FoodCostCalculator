package com.erdees.foodcostcalc.ui.screens.dishes.forms.newcomponent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.Preferences
import com.erdees.foodcostcalc.domain.model.units.MeasurementUnit
import com.erdees.foodcostcalc.domain.model.units.UnitCategory
import com.erdees.foodcostcalc.utils.UnitsUtils
import com.erdees.foodcostcalc.utils.Utils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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

    val productCreationUnitDropdownExpanded: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val productAdditionUnitDropdownExpanded: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private val _formData = MutableStateFlow(NewProductFormData())
    val formData: StateFlow<NewProductFormData> = _formData

    val isAddButtonEnabled = formData.map {
        it.purchasePrice.toDoubleOrNull() != null &&
                it.quantityAddedToDishUnit != null &&
                it.purchaseUnit != null &&
                it.quantityAddedToDish.toDoubleOrNull() != null
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    fun updateFormData(newValue: NewProductFormData) {
        _formData.update { newValue }
    }

    val productCreationUnits = MutableStateFlow<Set<MeasurementUnit>>(setOf())

    val productAdditionUnits = formData
        .distinctUntilChanged { old, new ->
            old.purchaseUnit == new.purchaseUnit
        }.map { formData ->
            val unitType = UnitsUtils.getUnitType(formData.purchaseUnit)
            unitType?.let { getUnitList(it) } ?: emptySet()
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    init {
        viewModelScope.launch {
            productAdditionUnits
                .distinctUntilChanged(areEquivalent = { old, new -> old == new })
                .filter { it.isNotEmpty() }
                .collect { availableUnits ->
                    if (formData.value.quantityAddedToDishUnit == null) {
                        _formData.update { it.copy(quantityAddedToDishUnit = availableUnits.first()) }
                    }
                }
        }
    }

    /**
     * Function used to prepare selection of units for the product creation.
     * */
    fun getProductCreationUnits() {
        viewModelScope.launch {
            val metricUsed = preferences.metricUsed.first()
            val imperialUsed = preferences.imperialUsed.first()
            productCreationUnits.update { Utils.getUnitsSet(metricUsed, imperialUsed) }
        }
    }

    /** Function used to prepare selection of units for the product addition,
     *  This is called every time product creation unit changes to provide relevant units in other dropdown.
     * */
    private suspend fun getUnitList(unitType: UnitCategory): Set<MeasurementUnit> {
        val metricUnits = preferences.metricUsed.first()
        val imperialUnits = preferences.imperialUsed.first()
        return Utils.generateUnitSet(
            unitType,
            metricUnits,
            imperialUnits
        )
    }

    fun onAddIngredientClick(){
        _formData.value = NewProductFormData()
    }
}