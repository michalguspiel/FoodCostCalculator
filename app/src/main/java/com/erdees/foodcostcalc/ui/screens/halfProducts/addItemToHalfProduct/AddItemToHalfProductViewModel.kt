package com.erdees.foodcostcalc.ui.screens.halfProducts.addItemToHalfProduct

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.Preferences
import com.erdees.foodcostcalc.data.model.associations.ProductHalfProduct
import com.erdees.foodcostcalc.data.repository.HalfProductRepository
import com.erdees.foodcostcalc.data.repository.ProductRepository
import com.erdees.foodcostcalc.domain.mapper.Mapper.toProductDomain
import com.erdees.foodcostcalc.domain.model.ScreenState
import com.erdees.foodcostcalc.domain.model.halfProduct.HalfProductDomain
import com.erdees.foodcostcalc.domain.model.product.ProductDomain
import com.erdees.foodcostcalc.utils.UnitsUtils
import com.erdees.foodcostcalc.utils.Utils
import com.erdees.foodcostcalc.utils.onNumericValueChange
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AddItemToHalfProductViewModel : ViewModel(), KoinComponent {

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

  private var metricUnits = preferences.metricUsed
  private var imperialUnits = preferences.imperialUsed

  private var _units = MutableStateFlow<Set<String>>(setOf())
  val units: StateFlow<Set<String>> = _units

  private var _selectedUnit = MutableStateFlow("")
  val selectedUnit: StateFlow<String> = _selectedUnit

  fun selectUnit(unit: String) {
    _selectedUnit.value = unit
  }

  private var halfProductUnitType: String? = null

  /** Represents type of the unit such as weight, volume or simply a piece of a product.*/
  private var unitType: String? = null

  private fun updateUnitList() {
    _units.value = Utils.generateUnitSet(
      unitType,
      metricUnits,
      imperialUnits
    )
    _selectedUnit.value = _units.value.firstOrNull() ?: ""
  }

  fun initializeWith(halfProductDomain: HalfProductDomain) {
    halfProductUnitType = UnitsUtils.getUnitType(halfProductDomain.halfProductUnit) ?: ""
  }

  fun selectProduct(product: ProductDomain?) {
    _selectedProduct.value = product
    unitType = UnitsUtils.getUnitType(product?.unit)
    updateUnitList()
  }

  fun addHalfProduct(halfProductDomain: HalfProductDomain) {
    val pieceQuantity = if (pieceQuantityNeeded()) {
      pieceWeight.value.toDouble()
    } else {
      1.0
    }

    val productHalfProduct = ProductHalfProduct(
      productHalfProductId = 0,
      productId = selectedProduct.value?.id ?: 0,
      halfProductId = halfProductDomain.id,
      quantity = quantity.value.toDouble(),
      quantityUnit = selectedUnit.value,
      weightPiece = pieceQuantity
    )

    _screenState.value = ScreenState.Loading
    viewModelScope.launch {
      try {
        halfProductRepository.addProductHalfProduct(productHalfProduct)
        _screenState.value = ScreenState.Success
      } catch (e: Exception) {
        _screenState.value = ScreenState.Error(Error(e.message))
      }
    }
  }

  fun pieceQuantityNeeded(): Boolean {
    return selectedProduct.value?.unit == "per piece" && halfProductUnitType != "piece"
  }
}
