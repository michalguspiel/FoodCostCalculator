package com.erdees.foodcostcalc.ui.screens.halfProducts.editHalfProduct

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.Preferences
import com.erdees.foodcostcalc.data.repository.HalfProductRepository
import com.erdees.foodcostcalc.domain.mapper.Mapper.toHalfProductBase
import com.erdees.foodcostcalc.domain.mapper.Mapper.toHalfProductDomain
import com.erdees.foodcostcalc.domain.mapper.Mapper.toProductHalfProduct
import com.erdees.foodcostcalc.domain.model.InteractionType
import com.erdees.foodcostcalc.domain.model.ScreenState
import com.erdees.foodcostcalc.domain.model.ScreenState.Interaction
import com.erdees.foodcostcalc.domain.model.UsedItem
import com.erdees.foodcostcalc.domain.model.halfProduct.HalfProductDomain
import com.erdees.foodcostcalc.domain.model.product.UsedProductDomain
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

class EditHalfProductViewModel : ViewModel(), KoinComponent {

    private val halfProductRepository: HalfProductRepository by inject()
    private val preferences: Preferences by inject()

    val currency = preferences.currency.stateIn(viewModelScope, SharingStarted.Lazily, null)

    private var _screenState: MutableStateFlow<ScreenState> = MutableStateFlow(ScreenState.Idle)
    val screenState: StateFlow<ScreenState> = _screenState

    private var currentlyEditedItem: MutableStateFlow<UsedItem?> = MutableStateFlow(null)

    private var _editableQuantity: MutableStateFlow<String> = MutableStateFlow("")
    val editableQuantity: StateFlow<String> = _editableQuantity

    fun setEditableQuantity(value: String) {
        onNumericValueChange(value, _editableQuantity)
    }

    private var _editableName: MutableStateFlow<String> = MutableStateFlow("")
    val editableName: StateFlow<String> = _editableName

    fun updateName(value: String) {
        _editableName.value = value
    }

    fun setInteraction(interaction: InteractionType) {
        when (interaction) {
            is InteractionType.EditItem -> {
                currentlyEditedItem.value = interaction.usedItem
                _editableQuantity.value = interaction.usedItem.quantity.toString()
            }

            is InteractionType.EditName ->
                _editableName.value = halfProduct.value?.name ?: ""

            else -> {}
        }
        _screenState.value = Interaction(interaction)
    }

    fun resetScreenState() {
        _screenState.value = ScreenState.Idle
    }


    private var _halfProduct = MutableStateFlow<HalfProductDomain?>(null)
    val halfProduct: StateFlow<HalfProductDomain?> = _halfProduct

    private var originalProducts: List<UsedProductDomain> = listOf()

    val usedItems: StateFlow<List<UsedProductDomain>> = halfProduct.map {
        it?.products ?: listOf()
    }.stateIn(viewModelScope, SharingStarted.Lazily, listOf())

    fun initializeWith(id: Long) {
        _screenState.update { ScreenState.Loading() }
        viewModelScope.launch {
            try {
                val halfProduct = halfProductRepository.getCompleteHalfProduct(id)
                    .flowOn(Dispatchers.IO)
                    .first()
                with(halfProduct.toHalfProductDomain()) {
                    _halfProduct.value = this
                    originalProducts = this.products
                }
                _screenState.update { ScreenState.Idle }
            } catch (e: Exception) {
                _screenState.update { ScreenState.Error(Error(e)) }
            }
        }
    }

    fun saveName() {
        val value = editableName.value
        _halfProduct.value = _halfProduct.value?.copy(name = value)
        resetScreenState()
    }

    /**
     * Removes [UsedProductDomain] from the temporary list of items. Requires saving to persist.
     *
     * @param item The item to remove.
     * */
    fun removeItem(item: UsedItem) {
        val halfProduct = halfProduct.value ?: return
        _halfProduct.value = halfProduct.copy(products = halfProduct.products.filter { it != item })
    }

    fun deleteHalfProduct(id: Long) {
        _screenState.value = ScreenState.Loading()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                halfProductRepository.deleteHalfProduct(id)
                _screenState.value = ScreenState.Success()
            } catch (e: Exception) {
                _screenState.value = ScreenState.Error(Error(e.message))
            }
        }
    }

    fun updateProductQuantity() {
        val usedItem = currentlyEditedItem.value ?: return
        val quantity = editableQuantity.value.toDoubleOrNull() ?: return
        val item = usedItem as? UsedProductDomain ?: return
        val halfProduct = halfProduct.value ?: return
        val index = halfProduct.products.indexOf(item)
        if (index != -1) {
            val updatedProduct = item.copy(quantity = quantity)
            _halfProduct.value = _halfProduct.value?.copy(
                products = halfProduct.products.toMutableList().apply { set(index, updatedProduct) }
            )
        }
        resetScreenState()
    }

    fun saveHalfProduct() {
        val halfProduct = halfProduct.value ?: return
        _screenState.value = ScreenState.Loading()
        viewModelScope.launch(Dispatchers.Default) {
            val editedProducts =
                halfProduct.products.filterNot { it in originalProducts }
                    .map { it.toProductHalfProduct() }
            val removedProducts = originalProducts.filterNot {
                it.id in halfProduct.products.map { product -> product.id }
            }.map { it.toProductHalfProduct() }
            try {
                withContext(Dispatchers.IO) {
                    editedProducts.forEach { halfProductRepository.updateProductHalfProduct(it) }
                    removedProducts.forEach { halfProductRepository.deleteProductHalfProduct(it) }

                    halfProductRepository.updateHalfProduct(halfProduct.toHalfProductBase())
                }
                _screenState.value = ScreenState.Success()
            } catch (e: Exception) {
                _screenState.value = ScreenState.Error(Error(e.message))
            }
        }
    }
}
