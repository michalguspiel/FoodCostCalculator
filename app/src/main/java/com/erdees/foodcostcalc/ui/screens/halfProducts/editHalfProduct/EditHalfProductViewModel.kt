package com.erdees.foodcostcalc.ui.screens.halfProducts.editHalfProduct

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.Preferences
import com.erdees.foodcostcalc.data.repository.AnalyticsRepository
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
import com.erdees.foodcostcalc.ui.navigation.FCCScreen.Companion.HALF_PRODUCT_ID_KEY
import com.erdees.foodcostcalc.utils.Constants
import com.erdees.foodcostcalc.utils.MyDispatchers
import com.erdees.foodcostcalc.utils.UnsavedChangesValidator
import com.erdees.foodcostcalc.utils.onNumericValueChange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class EditHalfProductViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel(),
    KoinComponent {

    private val halfProductRepository: HalfProductRepository by inject()
    private val preferences: Preferences by inject()
    private val analyticsRepository: AnalyticsRepository by inject()
    private val myDispatchers: MyDispatchers by inject()

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

    private var _lastRemovedItem: MutableStateFlow<UsedItem?> = MutableStateFlow(null)
    val lastRemovedItem: StateFlow<UsedItem?> = _lastRemovedItem

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
        .onStart { fetchHalfProduct() }
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            null
        )

    private var originalHalfProduct: HalfProductDomain? = null
    private var originalProducts: List<UsedProductDomain> = listOf()

    val usedItems: StateFlow<List<UsedProductDomain>> = halfProduct.map {
        it?.products ?: listOf()
    }.stateIn(viewModelScope, SharingStarted.Lazily, listOf())

    private fun fetchHalfProduct() {
        _screenState.update { ScreenState.Loading<Nothing>() }
        viewModelScope.launch {
            try {
                val halfProductId = savedStateHandle.get<Long>(HALF_PRODUCT_ID_KEY)
                    ?: throw NullPointerException("Failed to fetch half product due to missing id in savedStateHandle")

                Timber.i("Fetching half product with ID: $halfProductId")
                val halfProduct = halfProductRepository.getCompleteHalfProduct(halfProductId)
                    .flowOn(myDispatchers.ioDispatcher)
                    .first()
                with(halfProduct.toHalfProductDomain()) {
                    _halfProduct.value = this
                    originalHalfProduct = this
                    originalProducts = this.products
                    _editableName.value = this.name
                }
                _screenState.update { ScreenState.Idle }
            } catch (e: Exception) {
                Timber.e(e, "Error fetching half product")
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
        _lastRemovedItem.update { item }
    }

    private fun hasUnsavedChanges(): Boolean {
        val halfProductChanged =
            UnsavedChangesValidator.hasUnsavedChanges(originalHalfProduct, _halfProduct.value)
        val productsChanged =
            UnsavedChangesValidator.hasListChanges(originalProducts, _halfProduct.value?.products)
        return halfProductChanged || productsChanged
    }

    /**
     * Handles back navigation with unsaved changes check
     *
     * @param navigate The navigation action to perform if confirmed or no unsaved changes
     */
    fun handleBackNavigation(navigate: () -> Unit) {
        if (hasUnsavedChanges()) {
            _screenState.update { Interaction(InteractionType.UnsavedChangesConfirmation) }
        } else {
            navigate()
        }
    }

    /**
     * Called when user confirms to discard changes in the unsaved changes dialog
     */
    fun discardChanges(navigate: () -> Unit) {
        navigate()
        resetScreenState()
    }

    /**
     * Called when user confirms to save changes in the unsaved changes dialog
     */
    fun saveAndNavigate() {
        saveHalfProduct()
    }

    fun onDeleteHalfProductClick() {
        val halfProduct = _halfProduct.value ?: return
        analyticsRepository.logEvent(Constants.Analytics.HalfProducts.DELETE, null)
        _screenState.update {
            Interaction(
                InteractionType.DeleteConfirmation(halfProduct.id, halfProduct.name)
            )
        }
    }

    fun confirmDelete(halfProductId: Long) {
        _screenState.value = ScreenState.Loading<Nothing>()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                halfProductRepository.deleteHalfProduct(halfProductId)
                analyticsRepository.logEvent(Constants.Analytics.HalfProducts.DELETED, null)
                _screenState.value = ScreenState.Success<Nothing>()
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
        _screenState.value = ScreenState.Loading<Nothing>()
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
                _screenState.value = ScreenState.Success<Nothing>()
            } catch (e: Exception) {
                _screenState.value = ScreenState.Error(Error(e.message))
            }
        }
    }

    /**
     * Restores the last removed item to the half-product and clears lastRemovedItem.
     */
    fun undoRemoveItem() {
        val item = lastRemovedItem.value as? UsedProductDomain ?: return
        _halfProduct.update { halfProduct ->
            halfProduct?.copy(
                products = halfProduct.products.toMutableList().apply { add(item) }
            )
        }
        clearLastRemovedItem()
    }

    fun clearLastRemovedItem() {
        _lastRemovedItem.update { null }
    }
}
