package com.erdees.foodcostcalc.ui.screens.products.editProduct

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.Preferences
import com.erdees.foodcostcalc.data.model.local.ProductBase
import com.erdees.foodcostcalc.data.repository.ProductRepository
import com.erdees.foodcostcalc.domain.mapper.Mapper.toEditableProductDomain
import com.erdees.foodcostcalc.domain.mapper.Mapper.toProductBase
import com.erdees.foodcostcalc.domain.mapper.Mapper.toProductDomain
import com.erdees.foodcostcalc.domain.model.InteractionType
import com.erdees.foodcostcalc.domain.model.ScreenState
import com.erdees.foodcostcalc.domain.model.product.EditableProductDomain
import com.erdees.foodcostcalc.utils.onNumericValueChange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class EditProductViewModel : ViewModel(), KoinComponent {

    private val productRepository: ProductRepository by inject()
    private val preferences: Preferences by inject()

    private var _screenState: MutableStateFlow<ScreenState> = MutableStateFlow(ScreenState.Idle)
    val screenState: StateFlow<ScreenState> = _screenState

    fun resetScreenState() {
        _screenState.value = ScreenState.Idle
    }

    private var _product = MutableStateFlow<EditableProductDomain?>(null)
    val product: StateFlow<EditableProductDomain?> = _product

    private var _editableName: MutableStateFlow<String> = MutableStateFlow("")
    val editableName: StateFlow<String> = _editableName

    val showTaxPercent: StateFlow<Boolean> = preferences.showProductTax.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        false
    )

    fun initializeWith(productId: Long) {
        _screenState.update { ScreenState.Loading() }
        viewModelScope.launch {
            try {
                val product = productRepository.getProduct(productId)
                    .flowOn(Dispatchers.IO)
                    .first()
                with(product.toProductDomain()) {
                    _product.value = this.toEditableProductDomain()
                    _editableName.value = this.name
                }
                _screenState.update { ScreenState.Idle }
            } catch (e: Exception) {
                _screenState.update {
                    ScreenState.Error(Error(e))
                }
            }
        }
    }

    fun updateName(value: String) {
        _editableName.value = value
    }

    val saveNameButtonEnabled = editableName.map {
        it.isNotBlank()
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)


    fun saveName() {
        _product.value = _product.value?.copy(name = _editableName.value)
        resetScreenState()
    }

    fun updatePrice(price: String) {
        val newPrice = onNumericValueChange(
            newValue = price,
            oldValue = product.value?.pricePerUnit.toString()
        )
        _product.value = _product.value?.copy(pricePerUnit = newPrice)
    }

    fun updateTax(tax: String) {
        val newTax = onNumericValueChange(
            newValue = tax,
            oldValue = product.value?.tax.toString()
        )
        _product.value = _product.value?.copy(tax = newTax)
    }

    fun updateWaste(waste: String) {
        val newWaste = onNumericValueChange(
            newValue = waste,
            oldValue = product.value?.waste.toString()
        )
        _product.value = _product.value?.copy(waste = newWaste)
    }

    fun setInteractionEditName() {
        _screenState.value = ScreenState.Interaction(InteractionType.EditName)
    }

    fun deleteProduct(id: Long) {
        _screenState.value = ScreenState.Loading()
        try {
            viewModelScope.launch(Dispatchers.IO) {
                productRepository.deleteProduct(id)
                _screenState.value = ScreenState.Success()
            }
        } catch (e: Exception) {
            _screenState.value = ScreenState.Error(Error(e))
        }
    }

    val saveButtonEnabled = product.map {
        it?.name?.isNotBlank() == true &&
                it.pricePerUnit.toDoubleOrNull() != null &&
                it.waste.toDoubleOrNull() != null &&
                it.tax.toDoubleOrNull() != null
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    fun save() {
        _screenState.value = ScreenState.Loading()
        try {
            viewModelScope.launch(Dispatchers.Default) {
                val newProduct = product.value?.toProductBase()
                if (newProduct == null) {
                    _screenState.value = ScreenState.Error(Error(NumberFormatException()))
                    return@launch
                }
                updateProductInRepository(newProduct)
                _screenState.value = ScreenState.Success()
            }
        } catch (e: Exception) {
            _screenState.value = ScreenState.Error(Error(e))
        }
    }

    /**
     * Switches to IO dispatcher and updates product in repository
     * */
    private suspend fun updateProductInRepository(productBase: ProductBase) {
        withContext(Dispatchers.IO) {
            productRepository.editProduct(productBase)
        }
    }
}