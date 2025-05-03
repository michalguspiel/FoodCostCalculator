package com.erdees.foodcostcalc.ui.screens.dishes.addItemToDish

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.Preferences
import com.erdees.foodcostcalc.data.model.associations.HalfProductDish
import com.erdees.foodcostcalc.data.model.associations.ProductDish
import com.erdees.foodcostcalc.data.repository.HalfProductRepository
import com.erdees.foodcostcalc.data.repository.ProductRepository
import com.erdees.foodcostcalc.domain.mapper.Mapper.toHalfProductDomain
import com.erdees.foodcostcalc.domain.mapper.Mapper.toProductDomain
import com.erdees.foodcostcalc.domain.model.Item
import com.erdees.foodcostcalc.domain.model.ScreenState
import com.erdees.foodcostcalc.domain.model.halfProduct.HalfProductDomain
import com.erdees.foodcostcalc.domain.model.product.ProductDomain
import com.erdees.foodcostcalc.utils.MyDispatchers
import com.erdees.foodcostcalc.utils.UnitsUtils
import com.erdees.foodcostcalc.utils.Utils.generateUnitSet
import com.erdees.foodcostcalc.utils.onNumericValueChange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.Lazily
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AddItemToDishViewModel : ViewModel(), KoinComponent {

    private val dispatchers: MyDispatchers by inject()
    private val productRepository: ProductRepository by inject()
    private val halfProductRepository: HalfProductRepository by inject()
    private val sharedPreferences: Preferences by inject()

    private var _screenState: MutableStateFlow<ScreenState> = MutableStateFlow(ScreenState.Idle)
    val screenState: StateFlow<ScreenState> = _screenState

    fun resetScreenState() {
        _screenState.value = ScreenState.Idle
    }

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

    private var _selectedItem: MutableStateFlow<Item?> = MutableStateFlow(
        when (selectedTab.value) {
            SelectedTab.ADD_PRODUCT -> products.value.firstOrNull()
            SelectedTab.ADD_HALF_PRODUCT -> halfProducts.value.firstOrNull()
        }
    )
    val selectedItem: StateFlow<Item?> = _selectedItem

    fun selectItem(item: Item?) {
        _selectedItem.value = item
        when (item) {
            is ProductDomain -> setProductUnitType(item)
            is HalfProductDomain -> setHalfProductUnitType(item)
            else -> {
                unitType = null
            }
        }
        updateUnitList()
    }

    private var _quantity = MutableStateFlow("")
    val quantity: StateFlow<String> = _quantity

    fun setQuantity(newValue: String) {
        onNumericValueChange(newValue, _quantity)
    }

    private var _units = MutableStateFlow<Set<String>>(setOf())
    val units: StateFlow<Set<String>> = _units

    private var _selectedUnit = MutableStateFlow("")
    val selectedUnit: StateFlow<String> = _selectedUnit

    fun selectUnit(unit: String) {
        _selectedUnit.value = unit
    }

    /** Represents type of the unit such as weight, volume or simply a piece of a product.*/
    private var unitType: UnitsUtils.UnitType? = null

    private fun updateUnitList() {
        viewModelScope.launch(dispatchers.ioDispatcher) {
            val metricUnits = sharedPreferences.metricUsed.first()
            val imperialUnits = sharedPreferences.imperialUsed.first()
            withContext(dispatchers.mainDispatcher) {
                _units.value = generateUnitSet(
                    unitType,
                    metricUnits,
                    imperialUnits
                )
                _selectedUnit.value = _units.value.firstOrNull() ?: ""
            }
        }
    }

    private fun setProductUnitType(productDomain: ProductDomain) {
        unitType = UnitsUtils.getUnitType(productDomain.unit)
    }

    private fun setHalfProductUnitType(halfProductDomain: HalfProductDomain) {
        unitType = UnitsUtils.getUnitType(halfProductDomain.halfProductUnit)
    }

    val addButtonEnabled: StateFlow<Boolean> =
        combine(
            quantity,
            selectedUnit,
            selectedItem
        ) { quantity, selectedUnit, selectedItem ->
            quantity.toDoubleOrNull() != null &&
                    selectedUnit.isNotEmpty() &&
                    selectedItem != null
        }.stateIn(
            viewModelScope,
            Lazily,
            false
        )

    fun addItem(dishId: Long) {
        when (selectedTab.value) {
            SelectedTab.ADD_PRODUCT -> addProductToDish(dishId)
            SelectedTab.ADD_HALF_PRODUCT -> addHalfProductToDish(dishId)
        }
    }

    private fun addProductToDish(dishId: Long) {
        val productDish = ProductDish(
            productDishId = 0,
            productId = selectedItem.value?.id ?: 0,
            dishId = dishId,
            quantity = quantity.value.toDouble(),
            quantityUnit = selectedUnit.value
        )
        _screenState.value = ScreenState.Loading()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                productRepository.addProductDish(productDish)
                _screenState.value = ScreenState.Success()
            } catch (e: Exception) {
                _screenState.value = ScreenState.Error(Error(e.message))
            }
        }
    }

    private fun addHalfProductToDish(dishId: Long) {
        val halfProductDish = HalfProductDish(
            halfProductDishId = 0,
            halfProductId = selectedItem.value?.id ?: 0,
            dishId = dishId,
            quantity = quantity.value.toDouble(),
            quantityUnit = selectedUnit.value
        )
        _screenState.value = ScreenState.Loading()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                halfProductRepository.addHalfProductDish(halfProductDish)
                _screenState.value = ScreenState.Success()
            } catch (e: Exception) {
                _screenState.value = ScreenState.Error(Error(e.message))
            }
        }
    }
}
