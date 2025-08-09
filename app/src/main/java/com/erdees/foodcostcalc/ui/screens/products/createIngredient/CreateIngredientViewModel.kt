package com.erdees.foodcostcalc.ui.screens.products.createIngredient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.Preferences
import com.erdees.foodcostcalc.domain.model.InteractionType
import com.erdees.foodcostcalc.domain.model.ScreenState
import com.erdees.foodcostcalc.domain.model.product.InputMethod
import com.erdees.foodcostcalc.domain.model.units.MeasurementUnit
import com.erdees.foodcostcalc.domain.usecase.CreateProductUseCase
import com.erdees.foodcostcalc.ui.screens.products.EditableProductUiState
import com.erdees.foodcostcalc.ui.screens.products.PackagePriceState
import com.erdees.foodcostcalc.ui.screens.products.UnitPriceState
import com.erdees.foodcostcalc.ui.screens.products.delegates.NewProductFormBridgeDelegate
import com.erdees.foodcostcalc.ui.screens.products.delegates.PackagePricingDelegate
import com.erdees.foodcostcalc.ui.screens.products.delegates.ProductFormDelegate
import com.erdees.foodcostcalc.ui.screens.products.delegates.UnitPricingDelegate
import com.erdees.foodcostcalc.utils.Utils.formatResultAndCheckCommas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.text.DecimalFormat

class CreateIngredientViewModel : ViewModel(), KoinComponent {

    private val preferences: Preferences by inject()
    private val createProductUseCase: CreateProductUseCase by inject()

    private val productFormDelegate = ProductFormDelegate(preferences, viewModelScope)
    private val packagePricingDelegate = PackagePricingDelegate(viewModelScope)
    private val unitPricingDelegate = UnitPricingDelegate(viewModelScope)

    private val bridgeDelegate = NewProductFormBridgeDelegate(
        productFormDelegate,
        packagePricingDelegate,
        unitPricingDelegate,
        viewModelScope
    )

    private val _screenState = MutableStateFlow<ScreenState>(ScreenState.Idle)
    val screenState: StateFlow<ScreenState> = _screenState

    val currency = preferences.currency.stateIn(viewModelScope, SharingStarted.Lazily, null)

    val units: StateFlow<Set<MeasurementUnit>> = productFormDelegate.units
    val showTaxField: StateFlow<Boolean> = productFormDelegate.showTaxField

    val uiState: StateFlow<EditableProductUiState> = bridgeDelegate.toEditableProductUiState()

    val isSaveButtonEnabled: StateFlow<Boolean> = combine(
        productFormDelegate.createBaseValidation(),
        bridgeDelegate.createValidation()
    ) { baseValid, formValid ->
        baseValid && formValid
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun onNameChanged(newName: String) = productFormDelegate.updateName(newName)
    fun onTaxChanged(newTax: String) = productFormDelegate.updateTax(newTax)
    fun onWasteChanged(newWaste: String) = productFormDelegate.updateWaste(newWaste)
    fun togglePriceMode() {
        val currentMethod = uiState.value.let { state ->
            when (state) {
                is PackagePriceState -> InputMethod.PACKAGE
                is UnitPriceState -> InputMethod.UNIT
            }
        }
        val newMethod = when (currentMethod) {
            InputMethod.PACKAGE -> InputMethod.UNIT
            InputMethod.UNIT -> InputMethod.PACKAGE
        }
        productFormDelegate.setInputMethod(newMethod)
    }

    fun onPackagePriceChanged(newPrice: String) = packagePricingDelegate.updatePackagePrice(newPrice)
    fun onPackageQuantityChanged(newQuantity: String) = packagePricingDelegate.updatePackageQuantity(newQuantity)
    fun onPackageUnitChanged(newUnit: MeasurementUnit) = packagePricingDelegate.updatePackageUnit(newUnit)

    fun onUnitPriceChanged(newPrice: String) = unitPricingDelegate.updateUnitPrice(newPrice)
    fun onUnitPriceUnitChanged(newUnit: MeasurementUnit) = unitPricingDelegate.updateUnitPriceUnit(newUnit)

    fun resetScreenState() {
        _screenState.value = ScreenState.Idle
    }

    fun saveIngredient() {
        val currentState = uiState.value
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
                        _screenState.value = ScreenState.Error(Error(error.message ?: "Unknown error"))
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
        if (totalQuantity == 0.0) return
        val result = (100 * wasteQuantity) / totalQuantity
        onWasteChanged(formatResultAndCheckCommas(result))
        resetScreenState()
    }

    fun getCalculatedUnitPrice(): String? {
        val current = uiState.value
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

    private fun resetFormFields() {
        productFormDelegate.reset()
        packagePricingDelegate.reset()
        unitPricingDelegate.reset()
    }
}
