package com.erdees.foodcostcalc.ui.screens.halfProducts.addItemToHalfProduct

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.Preferences
import com.erdees.foodcostcalc.data.model.local.associations.ProductHalfProduct
import com.erdees.foodcostcalc.data.repository.HalfProductRepository
import com.erdees.foodcostcalc.data.repository.ProductRepository
import com.erdees.foodcostcalc.domain.mapper.Mapper.toProductDomain
import com.erdees.foodcostcalc.domain.model.ScreenState
import com.erdees.foodcostcalc.domain.model.product.ProductDomain
import com.erdees.foodcostcalc.domain.model.units.MeasurementUnit
import com.erdees.foodcostcalc.domain.model.units.UnitCategory
import com.erdees.foodcostcalc.utils.MyDispatchers
import com.erdees.foodcostcalc.utils.UnitsUtils
import com.erdees.foodcostcalc.utils.Utils.generateUnitSet
import com.erdees.foodcostcalc.utils.onNumericValueChange
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AddItemToHalfProductViewModel : ViewModel(), KoinComponent {

    private val dispatchers: MyDispatchers by inject()
    private val preferences: Preferences by inject()
    private val productRepository: ProductRepository by inject()
    private val halfProductRepository: HalfProductRepository by inject()

    private var _screenState: MutableStateFlow<ScreenState> = MutableStateFlow(ScreenState.Idle)
    val screenState: StateFlow<ScreenState> = _screenState

    fun resetScreenState() {
        _screenState.value = ScreenState.Idle
    }

    val products: StateFlow<List<ProductDomain>> =
        productRepository.products.map { list ->
            list.map { it.toProductDomain() }
        }.stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            listOf()
        )

    private var _selectedProduct: MutableStateFlow<ProductDomain?> =
        MutableStateFlow(products.value.firstOrNull())
    val selectedProduct: StateFlow<ProductDomain?> = _selectedProduct

    private var _quantity = MutableStateFlow("")
    val quantity: StateFlow<String> = _quantity

    fun setQuantity(newValue: String) {
        onNumericValueChange(newValue, _quantity)
    }

    private var _pieceWeight = MutableStateFlow("")
    val pieceWeight: StateFlow<String> = _pieceWeight

    fun setPieceWeight(newValue: String) {
        onNumericValueChange(newValue, _pieceWeight)
    }

    private var _units = MutableStateFlow<Set<MeasurementUnit>>(setOf())
    val units: StateFlow<Set<MeasurementUnit>> = _units

    private var _selectedUnit: MutableStateFlow<MeasurementUnit?> = MutableStateFlow(null)
    val selectedUnit: StateFlow<MeasurementUnit?> = _selectedUnit

    fun selectUnit(unit: MeasurementUnit) {
        _selectedUnit.value = unit
    }

    private var halfProductUnitType: UnitCategory? = null

    /** Represents type of the unit such as weight, volume or simply a piece of a product.*/
    private var unitType: UnitCategory? = null

    private fun updateUnitList() {
        viewModelScope.launch(dispatchers.ioDispatcher) {
            val metricUnits = preferences.metricUsed.first()
            val imperialUnits = preferences.imperialUsed.first()
            withContext(dispatchers.mainDispatcher) {
                _units.value = generateUnitSet(
                    unitType,
                    metricUnits,
                    imperialUnits
                )
                _selectedUnit.value = _units.value.firstOrNull()
            }
        }
    }

    fun initializeWith(halfProductUnit: MeasurementUnit) {
        halfProductUnitType = UnitsUtils.getUnitType(halfProductUnit)
    }

    fun selectProduct(product: ProductDomain?) {
        _selectedProduct.value = product
        unitType = UnitsUtils.getUnitType(product?.unit)
        updateUnitList()
    }

    val addButtonEnabled: StateFlow<Boolean> =
        combine(
            quantity,
            selectedUnit,
            selectedProduct
        ) { quantity, selectedUnit, selectedProduct ->
            quantity.toDoubleOrNull() != null &&
                    selectedUnit != null &&
                    selectedProduct != null
        }.stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            false
        )

    fun addHalfProduct(id: Long) {

        val selectedUnit = selectedUnit.value ?: error("Selected unit cannot be null")

        val pieceQuantity = if (pieceQuantityNeeded()) {
            pieceWeight.value.toDoubleOrNull() ?: 1.0
        } else {
            1.0
        }

        val productHalfProduct = ProductHalfProduct(
            productHalfProductId = 0,
            productId = selectedProduct.value?.id ?: 0,
            halfProductId = id,
            quantity = quantity.value.toDouble(),
            quantityUnit = selectedUnit,
            weightPiece = pieceQuantity
        )

        _screenState.value = ScreenState.Loading<Nothing>()
        viewModelScope.launch {
            try {
                halfProductRepository.addProductHalfProduct(productHalfProduct)
                _screenState.value = ScreenState.Success(selectedProduct.value?.name)
            } catch (e: Exception) {
                _screenState.value = ScreenState.Error(Error(e.message))
            }
        }
    }

    fun pieceQuantityNeeded(): Boolean {
        return selectedProduct.value?.unit == MeasurementUnit.PIECE && halfProductUnitType != UnitCategory.COUNT
    }
}
