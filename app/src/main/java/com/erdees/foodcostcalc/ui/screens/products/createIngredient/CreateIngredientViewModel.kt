package com.erdees.foodcostcalc.ui.screens.products.createIngredient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.Preferences
import com.erdees.foodcostcalc.domain.model.InteractionType
import com.erdees.foodcostcalc.domain.model.ScreenState
import com.erdees.foodcostcalc.domain.model.units.MeasurementUnit
import com.erdees.foodcostcalc.domain.usecase.CreateProductUseCase
import com.erdees.foodcostcalc.ui.screens.products.EditableProductUiState
import com.erdees.foodcostcalc.ui.screens.products.PackagePriceState
import com.erdees.foodcostcalc.ui.screens.products.UnitPriceState
import com.erdees.foodcostcalc.utils.Utils
import com.erdees.foodcostcalc.utils.Utils.formatResultAndCheckCommas
import com.erdees.foodcostcalc.utils.onNumericValueChange
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharingStarted.Companion.Lazily
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.text.DecimalFormat

class CreateIngredientViewModel : ViewModel(), KoinComponent {

    private val preferences: Preferences by inject()
    private val createProductUseCase: CreateProductUseCase by inject()

    private val _uiState = MutableStateFlow<EditableProductUiState>(PackagePriceState())
    val uiState: StateFlow<EditableProductUiState> = _uiState

    private val _screenState = MutableStateFlow<ScreenState>(ScreenState.Idle)
    val screenState: StateFlow<ScreenState> = _screenState

    val currency = preferences.currency.stateIn(viewModelScope, Lazily, null)

    private val _units = MutableStateFlow<Set<MeasurementUnit>>(setOf())
    val units: StateFlow<Set<MeasurementUnit>> = _units.onStart {
        loadUnits()
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        setOf()
    )

    val showTaxField: StateFlow<Boolean> = preferences.showProductTax
        .onEach { initializeTaxField(it) }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            true
        )

    val isSaveButtonEnabled: StateFlow<Boolean> = combine(
        uiState,
        showTaxField
    ) { state, showTax ->
        when (state) {
            is PackagePriceState -> {
                state.name.isNotBlank() &&
                        state.packagePrice.toDoubleOrNull() != null &&
                        state.packageQuantity.toDoubleOrNull() != null &&
                        state.waste.toDoubleOrNull() != null &&
                        (!showTax || state.tax.toDoubleOrNull() != null)
            }

            is UnitPriceState -> {
                state.name.isNotBlank() &&
                        state.unitPrice.toDoubleOrNull() != null &&
                        state.waste.toDoubleOrNull() != null &&
                        (!showTax || state.tax.toDoubleOrNull() != null)
            }
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        false
    )

    private fun loadUnits() {
        viewModelScope.launch {
            val metricUsed = preferences.metricUsed.first()
            val imperialUsed = preferences.imperialUsed.first()
            _units.value = Utils.getCompleteUnitsSet(metricUsed, imperialUsed)
        }
    }

    fun resetScreenState() {
        _screenState.value = ScreenState.Idle
    }

    fun onNameChanged(newName: String) {
        _uiState.value = when (val current = _uiState.value) {
            is PackagePriceState -> current.copy(name = newName)
            is UnitPriceState -> current.copy(name = newName)
        }
    }

    fun onTaxChanged(newTax: String) {
        if (showTaxField.value) {
            _uiState.value = when (val current = _uiState.value) {
                is PackagePriceState -> current.copy(
                    tax = onNumericValueChange(
                        current.tax,
                        newTax
                    )
                )

                is UnitPriceState -> current.copy(tax = onNumericValueChange(current.tax, newTax))
            }
        }
    }

    fun onWasteChanged(newWaste: String) {
        _uiState.value = when (val current = _uiState.value) {
            is PackagePriceState -> current.copy(
                waste = onNumericValueChange(
                    current.waste,
                    newWaste
                )
            )

            is UnitPriceState -> current.copy(waste = onNumericValueChange(current.waste, newWaste))
        }
    }

    fun onPackagePriceChanged(newPrice: String) {
        val current = _uiState.value
        if (current is PackagePriceState) {
            updateStateWithPackageData(
                packagePriceState = current,
                quantity = current.packageQuantity,
                price = newPrice,
                unit = current.packageUnit
            )
        }
    }

    fun onPackageQuantityChanged(newQuantity: String) {
        val current = _uiState.value
        if (current is PackagePriceState) {
                updateStateWithPackageData(
                    packagePriceState = current,
                    quantity = newQuantity,
                    price = current.packagePrice,
                    unit = current.packageUnit
                )
        }
    }

    fun onPackageUnitChanged(newUnit: MeasurementUnit) {
        val current = _uiState.value
        if (current is PackagePriceState) {
            updateStateWithPackageData(
                packagePriceState = current,
                quantity = current.packageQuantity,
                price = current.packagePrice,
                unit = newUnit
            )
        }
    }

    fun onUnitPriceChanged(newPrice: String) {
        val current = _uiState.value
        if (current is UnitPriceState) {
            val newPrice = onNumericValueChange(current.unitPrice, newPrice)
            _uiState.value =
                current.copy(unitPrice = newPrice)
        }
    }

    fun onUnitPriceUnitChanged(newUnit: MeasurementUnit) {
        val current = _uiState.value
        if (current is UnitPriceState) {
            _uiState.value = current.copy(unitPriceUnit = newUnit)
        }
    }

    private fun initializeTaxField(show: Boolean) {
        if (show) {
            return
        }
        val zeroTax = "0.0"
        _uiState.value = when (val current = _uiState.value) {
            is PackagePriceState -> current.copy(tax = zeroTax)
            is UnitPriceState -> current.copy(tax = zeroTax)
        }
    }

    fun togglePriceMode() {
        val current = _uiState.value
        _uiState.value = when (current) {
            is PackagePriceState -> UnitPriceState(
                id = current.id,
                name = current.name,
                tax = current.tax,
                waste = current.waste,
                unitPrice = "",
                unitPriceUnit = MeasurementUnit.KILOGRAM
            )

            is UnitPriceState -> PackagePriceState(
                id = current.id,
                name = current.name,
                tax = current.tax,
                waste = current.waste,
                packagePrice = "",
                packageQuantity = "",
                packageUnit = MeasurementUnit.KILOGRAM,
                canonicalPrice = null,
                canonicalUnit = null
            )
        }
    }

    fun getCalculatedUnitPrice(): String? {
        val current = _uiState.value
        return if (current is PackagePriceState) {
            val price = current.packagePrice.toDoubleOrNull()
            val quantity = current.packageQuantity.toDoubleOrNull()
            if (price != null && quantity != null && quantity > 0) {
                val unitPrice = price / quantity
                val formatter = DecimalFormat("#.##")
                formatter.format(unitPrice)
            } else {
                null
            }
        } else {
            null
        }
    }

    fun saveIngredient() {
        val currentState = _uiState.value
        _screenState.value = ScreenState.Loading<Nothing>()

        viewModelScope.launch {
            try {
                val result = when (currentState) {
                    is PackagePriceState -> createProductUseCase(currentState)
                    is UnitPriceState -> createProductUseCase(currentState)
                }

                result.fold(
                    onSuccess = { product ->
                        _screenState.value = ScreenState.Success(product.name)
                        resetFormFields()
                    },
                    onFailure = { error ->
                        _screenState.value =
                            ScreenState.Error(Error(error.message ?: "Unknown error"))
                    }
                )
            } catch (e: Exception) {
                _screenState.value = ScreenState.Error(Error(e.message ?: "Unknown error"))
            }
        }
    }

    fun onCalculateWaste() {
        _screenState.value = ScreenState.Interaction(InteractionType.CalculateWaste)
    }

    fun calculateWaste(totalQuantity: Double?, wasteQuantity: Double?) {
        totalQuantity ?: return
        wasteQuantity ?: return
        val result = (100 * wasteQuantity) / totalQuantity
        onWasteChanged(formatResultAndCheckCommas(result))
        resetScreenState()
    }

    private fun resetFormFields() {
        _uiState.value = PackagePriceState()
    }

    private fun updateStateWithPackageData(
        packagePriceState: PackagePriceState,
        quantity: String,
        price: String,
        unit: MeasurementUnit,
    ) {
        val packageQuantity = quantity.toDoubleOrNull()
        val packagePrice = price.toDoubleOrNull()
        val (canonicalPrice, canonicalUnit) =
            if (packagePrice != null && packageQuantity != null) {
                unit.calculateCanonicalPrice(
                    packagePrice, packageQuantity
                )
            } else {
                Pair(null, null)
            }

        _uiState.value = packagePriceState.copy(
            packageUnit = unit,
            packageQuantity = quantity,
            packagePrice = price,
            canonicalPrice = canonicalPrice,
            canonicalUnit = canonicalUnit
        )
    }
}
