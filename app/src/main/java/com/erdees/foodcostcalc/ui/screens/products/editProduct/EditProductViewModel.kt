package com.erdees.foodcostcalc.ui.screens.products.editProduct

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.Preferences
import com.erdees.foodcostcalc.data.model.local.ProductBase
import com.erdees.foodcostcalc.data.repository.AnalyticsRepository
import com.erdees.foodcostcalc.data.repository.ProductRepository
import com.erdees.foodcostcalc.domain.mapper.Mapper.toEditableProductDomain
import com.erdees.foodcostcalc.domain.mapper.Mapper.toProductBase
import com.erdees.foodcostcalc.domain.mapper.Mapper.toProductDomain
import com.erdees.foodcostcalc.domain.model.InteractionType
import com.erdees.foodcostcalc.domain.model.ScreenState
import com.erdees.foodcostcalc.domain.model.product.EditableProductDomain
import com.erdees.foodcostcalc.ui.navigation.FCCScreen.Companion.PRODUCT_ID_KEY
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

class EditProductViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel(),
    KoinComponent {

    private val productRepository: ProductRepository by inject()
    private val preferences: Preferences by inject()
    private val analyticsRepository: AnalyticsRepository by inject()
    private val myDispatchers: MyDispatchers by inject()

    private var _screenState: MutableStateFlow<ScreenState> = MutableStateFlow(ScreenState.Idle)
    val screenState: StateFlow<ScreenState> = _screenState

    // Store the navigation action to be executed after confirmation
    private var pendingNavigation: (() -> Unit)? = null

    fun resetScreenState() {
        _screenState.value = ScreenState.Idle
    }

    private var _product = MutableStateFlow<EditableProductDomain?>(null)
    val product: StateFlow<EditableProductDomain?> = _product
        .onStart { fetchProduct() }
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            null
        )

    // Store original product state for comparison
    private var originalProduct: EditableProductDomain? = null

    private var _editableName: MutableStateFlow<String> = MutableStateFlow("")
    val editableName: StateFlow<String> = _editableName

    val showTaxPercent: StateFlow<Boolean> = preferences.showProductTax.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        false
    )

    private fun fetchProduct() {
        _screenState.update { ScreenState.Loading<Nothing>() }
        viewModelScope.launch {
            try {
                val productId = savedStateHandle.get<Long>(PRODUCT_ID_KEY)
                    ?: throw NullPointerException("Failed to fetch product due to missing id in savedStateHandle")

                Timber.i("Fetching product with ID: $productId")
                val product = productRepository.getProduct(productId)
                    .flowOn(myDispatchers.ioDispatcher)
                    .first()
                with(product.toProductDomain()) {
                    _product.value = this.toEditableProductDomain()
                    originalProduct = this.toEditableProductDomain() // Store original state
                    _editableName.value = this.name
                }
                _screenState.update { ScreenState.Idle }
            } catch (e: Exception) {
                Timber.e(e, "Error fetching product")
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

    val saveButtonEnabled = product.map {
        it?.name?.isNotBlank() == true &&
                it.pricePerUnit.toDoubleOrNull() != null &&
                it.waste.toDoubleOrNull() != null &&
                it.tax.toDoubleOrNull() != null
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    fun save() {
        _screenState.value = ScreenState.Loading<Nothing>()
        try {
            viewModelScope.launch(Dispatchers.Default) {
                val newProduct = product.value?.toProductBase()
                if (newProduct == null) {
                    _screenState.value = ScreenState.Error(Error(NumberFormatException()))
                    return@launch
                }
                updateProductInRepository(newProduct)
                _screenState.value = ScreenState.Success<Nothing>()

                // Clear pending navigation after successful save
                pendingNavigation = null
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

    fun onDeleteProductClick() {
        val product = _product.value ?: return
        analyticsRepository.logEvent(Constants.Analytics.Products.DELETE, null)
        _screenState.update {
            ScreenState.Interaction(InteractionType.DeleteConfirmation(product.id, product.name))
        }
    }

    fun deleteProduct(id: Long) {
        _screenState.value = ScreenState.Loading<Nothing>()
        try {
            viewModelScope.launch(Dispatchers.IO) {
                productRepository.deleteProduct(id)
                analyticsRepository.logEvent(Constants.Analytics.Products.DELETED, null)
                _screenState.value = ScreenState.Success<Nothing>()
            }
        } catch (e: Exception) {
            _screenState.value = ScreenState.Error(Error(e))
        }
    }

    /**
     * Checks if there are unsaved changes by comparing current product state with original state.
     *
     * @return true if there are unsaved changes, false otherwise
     */
    fun hasUnsavedChanges(): Boolean {
        return UnsavedChangesValidator.hasUnsavedChanges(originalProduct, _product.value)
    }

    /**
     * Handles back navigation with unsaved changes check
     *
     * @param navigate The navigation action to perform if confirmed or no unsaved changes
     */
    fun handleBackNavigation(navigate: () -> Unit) {
        if (hasUnsavedChanges()) {
            // Store the navigation action for later use
            pendingNavigation = navigate
            // Show confirmation dialog
            _screenState.update { ScreenState.Interaction(InteractionType.UnsavedChangesConfirmation) }
        } else {
            // No unsaved changes, proceed with navigation
            navigate()
        }
    }

    /**
     * Called when user confirms to discard changes in the unsaved changes dialog
     */
    fun discardChanges() {
        pendingNavigation?.invoke()
        pendingNavigation = null
        resetScreenState()
    }

    /**
     * Called when user confirms to save changes in the unsaved changes dialog
     */
    fun saveAndNavigate() {
        // Save and then navigate
        save()
        // The navigation will be handled in the LaunchedEffect in the UI that observes ScreenState.Success
    }
}