package com.erdees.foodcostcalc.ui.screens.dishes.addItemToDish

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.Preferences
import com.erdees.foodcostcalc.data.model.associations.ProductDish
import com.erdees.foodcostcalc.data.repository.HalfProductRepository
import com.erdees.foodcostcalc.data.repository.ProductRepository
import com.erdees.foodcostcalc.domain.mapper.Mapper.toHalfProductDomain
import com.erdees.foodcostcalc.domain.mapper.Mapper.toProductDomain
import com.erdees.foodcostcalc.domain.model.HalfProductDomain
import com.erdees.foodcostcalc.domain.model.Item
import com.erdees.foodcostcalc.domain.model.ProductDomain
import com.erdees.foodcostcalc.utils.UnitsUtils
import com.erdees.foodcostcalc.utils.Utils.changeUnitList
import com.erdees.foodcostcalc.utils.Utils.generateUnitSet
import com.erdees.foodcostcalc.utils.onNumericValueChange
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.Lazily
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AddItemToDishViewModel : ViewModel(), KoinComponent {

  private val productRepository: ProductRepository by inject()
  private val halfProductRepository: HalfProductRepository by inject()
  private val sharedPreferences: Preferences by inject()

  private var _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading

  val halfProducts: StateFlow<List<HalfProductDomain>> =
    halfProductRepository.halfProducts.map { list ->
      list.map { it.toHalfProductDomain() }
    }.stateIn(
      viewModelScope,
      Lazily,
      listOf()
    )

  val products: StateFlow<List<ProductDomain>> =
    productRepository.products.map { list ->
      list.map { it.toProductDomain() }
    }.stateIn(
      viewModelScope,
      Lazily,
      listOf()
    )

  private var _selectedTab = MutableStateFlow(SelectedTab.ADD_PRODUCT)
  val selectedTab: StateFlow<SelectedTab> = _selectedTab

  fun selectTab(tab: SelectedTab) {
    _selectedTab.value = tab
    when (tab) {
      SelectedTab.ADD_PRODUCT -> selectItem(products.value.firstOrNull())
      SelectedTab.ADD_HALF_PRODUCT -> selectItem(halfProducts.value.firstOrNull())
    }
    updateUnitList()
  }

  private var _selectedItem = MutableStateFlow(
    when (selectedTab.value) {
      SelectedTab.ADD_PRODUCT -> products.value.firstOrNull()
      SelectedTab.ADD_HALF_PRODUCT -> halfProducts.value.firstOrNull()
    }
  )
  val selectedItem: StateFlow<Item?> = _selectedItem

  fun selectItem(item: Item?) {
    _selectedItem.value = item
    when (item) {
      is ProductDomain -> setProductUnitType(products.value.indexOf(item))
      is HalfProductDomain -> setHalfProductUnitType(halfProducts.value.indexOf(item))
      else -> {
        unitType = ""
      }
    }
    updateUnitList()
  }

  private var _quantity = MutableStateFlow("")
  val quantity: StateFlow<String> = _quantity

  fun setQuantity(newValue: String) {
    onNumericValueChange(newValue, _quantity)
  }

  private var metricUnits = sharedPreferences.metricUsed
  private var imperialUnits = sharedPreferences.imperialUsed

  private var _units = MutableStateFlow<Set<String>>(setOf())
  val units: StateFlow<Set<String>> = _units

  private var _selectedUnit = MutableStateFlow("")
  val selectedUnit: StateFlow<String> = _selectedUnit

  fun selectUnit(unit: String) {
    _selectedUnit.value = unit
  }

  /** Represents type of the unit such as weight, volume or simply a piece of a product.*/
  private var unitType: String? = null

  private fun updateUnitList() {
    _units.value = generateUnitSet(
      unitType,
      metricUnits,
      imperialUnits
    )
    _selectedUnit.value = _units.value.firstOrNull() ?: ""
    Log.i(
      "AddItemToDishViewModel",
      "Unit list updated ${units.value} , selectedUnit: ${selectedUnit.value}"
    )
  }

  private fun setProductUnitType(position: Int) {
    unitType = UnitsUtils.getUnitType(
      products.value.getOrNull(position)?.unit
    )
  }

  private fun setHalfProductUnitType(position: Int) {
    unitType = UnitsUtils.getUnitType(
      halfProducts.value.getOrNull(position)?.halfProductUnit
    )
  }

  fun addItem(dishId: Long) {
    when (selectedTab.value) {
      SelectedTab.ADD_PRODUCT -> addProductToDish(dishId)
      SelectedTab.ADD_HALF_PRODUCT -> addHalfProductToDish(dishId)
    }
  }

  private fun addProductToDish(dishId: Long) {
    // This needs to create ProductDish object and add it to the database
    val productDish = ProductDish(
      productDishId = 0,
      productId = selectedItem.value?.id ?: 0,
      dishId = dishId,
      quantity = quantity.value.toDouble(),
      quantityUnit = selectedUnit.value
    )
    _isLoading.value = true
    viewModelScope.launch {
      productRepository.addProductDish(productDish)
    }
    _isLoading.value = false
  }

  private fun addHalfProductToDish(dishId: Long) {
    // TODO
    // This needs to create HalfProductDish object and add it to the database
  }
}
