package com.erdees.foodcostcalc.ui.screens.dishes.editDish

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.Preferences
import com.erdees.foodcostcalc.data.repository.AnalyticsRepository
import com.erdees.foodcostcalc.data.repository.DishRepository
import com.erdees.foodcostcalc.domain.mapper.Mapper.toDishBase
import com.erdees.foodcostcalc.domain.mapper.Mapper.toDishDomain
import com.erdees.foodcostcalc.domain.mapper.Mapper.toEditableRecipe
import com.erdees.foodcostcalc.domain.mapper.Mapper.toHalfProductDish
import com.erdees.foodcostcalc.domain.mapper.Mapper.toProductDish
import com.erdees.foodcostcalc.domain.model.InteractionType
import com.erdees.foodcostcalc.domain.model.ScreenState
import com.erdees.foodcostcalc.domain.model.UsedItem
import com.erdees.foodcostcalc.domain.model.dish.DishDomain
import com.erdees.foodcostcalc.domain.model.halfProduct.UsedHalfProductDomain
import com.erdees.foodcostcalc.domain.model.product.UsedProductDomain
import com.erdees.foodcostcalc.ext.toShareableText
import com.erdees.foodcostcalc.ui.navigation.FCCScreen.Companion.DISH_ID_KEY
import com.erdees.foodcostcalc.ui.screens.recipe.RecipeHandler
import com.erdees.foodcostcalc.ui.screens.recipe.RecipeUpdater
import com.erdees.foodcostcalc.ui.screens.recipe.RecipeViewMode
import com.erdees.foodcostcalc.utils.Constants
import com.erdees.foodcostcalc.utils.MyDispatchers
import com.erdees.foodcostcalc.utils.UnsavedChangesValidator
import com.erdees.foodcostcalc.utils.Utils
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
import timber.log.Timber

/**
 * Shared ViewModel between [DishDetailsScreen] and [RecipeScreen].
 * It was decided to share it in order to avoid passing data between screens.
 * */
class DishDetailsViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel(),
    KoinComponent {

    private val dishRepository: DishRepository by inject()
    private val preferences: Preferences by inject()
    private val analyticsRepository: AnalyticsRepository by inject()
    private val myDispatchers: MyDispatchers by inject()

    private var _screenState: MutableStateFlow<ScreenState> = MutableStateFlow(ScreenState.Idle)
    val screenState: StateFlow<ScreenState> = _screenState

    private var _dish = MutableStateFlow<DishDomain?>(null)
    val dish: StateFlow<DishDomain?> = _dish

    private var _editableName: MutableStateFlow<String> = MutableStateFlow("")
    val editableName: StateFlow<String> = _editableName

    private var _editableTotalPrice: MutableStateFlow<String> = MutableStateFlow("")
    val editableTotalPrice: StateFlow<String> = _editableTotalPrice

    // Store the navigation action to be executed after confirmation
    private var pendingNavigation: (() -> Unit)? = null

    // Original dish for comparison to detect unsaved changes
    private var originalDish: DishDomain? = null

    val currency = preferences.currency.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val recipeHandler: RecipeHandler = RecipeHandler(
        viewModelScope = viewModelScope,
        resetScreenState = { resetScreenState() },
        updateScreenState = { _screenState.value = it },
        updateDish = { _dish.value = it }
    )

    val recipe = recipeHandler.recipe
    val recipeViewModeState = recipeHandler.recipeViewModeState
    val recipeServings = recipeHandler.recipeServings
    val recipeEvent = recipeHandler.recipeEvent
    val recipeUpdater = RecipeUpdater(
        updatePrepTime = recipeHandler::updatePrepTime,
        updateCookTime = recipeHandler::updateCookTime,
        updateDescription = recipeHandler::updateDescription,
        updateTips = recipeHandler::updateTips,
        updateStep = recipeHandler::updateStep
    )

    init {
        fetchDish()
    }

    private fun fetchDish() {
        Timber.i("fetchDish() \n SavedStateHandle: $savedStateHandle, ${savedStateHandle.get<Long>("dishId")}")
        _screenState.update { ScreenState.Loading<Nothing>() }
        viewModelScope.launch {
            try {
                val id = savedStateHandle.get<Long>(DISH_ID_KEY) ?: throw NullPointerException("Failed to fetch dish due to missing id in savedStateHandle")
                val dish = dishRepository.getDish(id).flowOn(myDispatchers.ioDispatcher).first()
                with(dish.toDishDomain()) {
                    Timber.i("Fetched dish: $this")
                    if (this.recipe == null) {
                        recipeHandler.updateRecipeViewMode(RecipeViewMode.EDIT)
                    }
                    _dish.update { this }
                    // Store original dish for unsaved changes detection
                    originalDish = this.copy()
                    recipeHandler.updateRecipe(this.recipe.toEditableRecipe())
                    originalProducts = this.products
                    originalHalfProducts = this.halfProducts
                }
                _screenState.update { ScreenState.Idle }
            } catch (e: Exception) {
                _screenState.update { ScreenState.Error(Error(e)) }
            }
        }
    }

    fun updateName(value: String) {
        _editableName.value = value
    }

    private var _editableQuantity: MutableStateFlow<String> = MutableStateFlow("")
    val editableQuantity: StateFlow<String> = _editableQuantity

    fun updateQuantity(value: String) {
        onNumericValueChange(value, _editableQuantity)
    }

    private var _editableTax: MutableStateFlow<String> = MutableStateFlow("")
    val editableTax: StateFlow<String> = _editableTax

    fun updateTax(value: String) {
        onNumericValueChange(value, _editableTax)
    }

    private var _editableMargin: MutableStateFlow<String> = MutableStateFlow("")
    val editableMargin: StateFlow<String> = _editableMargin

    fun updateMargin(value: String) {
        onNumericValueChange(value, _editableMargin)
    }

    fun updateTotalPrice(value: String) {
        onNumericValueChange(value, _editableTotalPrice)
    }

    private var currentlyEditedItem: MutableStateFlow<UsedItem?> = MutableStateFlow(null)

    fun setInteraction(interaction: InteractionType) {
        when (interaction) {
            is InteractionType.EditItem -> {
                currentlyEditedItem.value = interaction.usedItem
                _editableQuantity.value = interaction.usedItem.quantity.toString()
            }

            is InteractionType.EditTax -> _editableTax.value = dish.value?.taxPercent.toString()

            is InteractionType.EditMargin -> _editableMargin.value =
                dish.value?.marginPercent.toString()

            is InteractionType.EditName -> _editableName.value = dish.value?.name ?: ""

            is InteractionType.EditTotalPrice -> {
                if (_dish.value?.foodCost == 0.00) {
                    return
                }
                val price = Utils.formatPriceWithoutSymbol(dish.value?.totalPrice, currency.value?.currencyCode)
                _editableTotalPrice.value = price
            }

            else -> {}
        }
        _screenState.value = ScreenState.Interaction(interaction)
    }

    fun resetScreenState() {
        _screenState.value = ScreenState.Idle
    }

    fun updateItemQuantity() {
        val value = editableQuantity.value.toDoubleOrNull()
        val item = currentlyEditedItem.value
        val dish = dish.value

        if (value == null || item == null || dish == null) {
            return
        }

        when (item) {
            is UsedProductDomain -> {
                val index = dish.products.indexOf(item)
                if (index != -1) {
                    val updatedItem = item.copy(quantity = value)
                    _dish.value = _dish.value?.copy(
                        products = dish.products.toMutableList().apply { set(index, updatedItem) })
                }
            }

            is UsedHalfProductDomain -> {
                val index = dish.halfProducts.indexOf(item)
                if (index != -1) {
                    val updatedItem = item.copy(quantity = value)
                    _dish.value = _dish.value?.copy(halfProducts = dish.halfProducts.toMutableList()
                        .apply { set(index, updatedItem) })
                }
            }
        }
        resetScreenState()
    }

    fun saveDishTax() {
        val value = editableTax.value.toDoubleOrNull()
        if (value == null) {
            resetScreenState()
            return
        }
        _dish.value = _dish.value?.copy(taxPercent = value)
        resetScreenState()
    }

    fun saveDishMargin() {
        val value = editableMargin.value.toDoubleOrNull()
        if (value == null) {
            resetScreenState()
            return
        }
        _dish.value = _dish.value?.copy(marginPercent = value)
        resetScreenState()
    }

    fun saveDishTotalPrice() {
        val newTotalPriceString = _editableTotalPrice.value
        val currentDish = _dish.value
        if (currentDish == null) {
            Timber.e("Current dish is null, cannot save total price.")
            resetScreenState() // Or handle error appropriately
            return
        }
        val newTotalPrice = newTotalPriceString.toDoubleOrNull()
        if (newTotalPrice == null) {
            Timber.e("Invalid total price format: $newTotalPriceString")
            _screenState.value = ScreenState.Error(Error("Invalid total price format.")) // Example error handling
            // Optionally, reset _editableTotalPrice.value or keep it for user correction
            return
        }
        _dish.value = currentDish.withUpdatedTotalPrice(newTotalPrice)
        resetScreenState()
    }

    fun saveDishName() {
        val value = editableName.value
        _dish.value = _dish.value?.copy(name = value)
        resetScreenState()
    }

    private var originalProducts: List<UsedProductDomain> = listOf()
    private var originalHalfProducts: List<UsedHalfProductDomain> = listOf()

    val items: StateFlow<List<UsedItem>> = dish.map {
        val products = it?.products ?: listOf()
        val halfProducts = it?.halfProducts ?: listOf()
        products + halfProducts
    }.stateIn(viewModelScope, SharingStarted.Lazily, listOf())

    /**
     * Removes item from the temporary list of items. Requires saving to persist.
     *
     * @param item The item to remove.
     * */
    fun removeItem(item: UsedItem) {
        val dish = dish.value ?: return
        when (item) {
            is UsedProductDomain -> _dish.value =
                dish.copy(products = dish.products.filter { it != item })

            is UsedHalfProductDomain -> _dish.value =
                dish.copy(halfProducts = dish.halfProducts.filter { it != item })
        }
    }

    /**
     * Checks if the dish has unsaved changes by comparing with the original state
     *
     * @return true if there are unsaved changes, false otherwise
     */
    fun hasUnsavedChanges(): Boolean {
        if (recipeHandler.hasRecipeChanges(originalDish?.recipe)) {
            return true
        }

        // Check if dish properties have changed
        if (!UnsavedChangesValidator.hasUnsavedChanges(originalDish, _dish.value)) {
            // Deep check for products and half-products changes
            val productsChanged = originalProducts != _dish.value?.products
            val halfProductsChanged = originalHalfProducts != _dish.value?.halfProducts

            return productsChanged || halfProductsChanged
        }

        return true
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
        saveDish()
        // The navigation will be handled in the LaunchedEffect in the UI that observes ScreenState.Success
    }

    /**
     * This function is responsible for saving the changes made to a dish.
     * It first sets the screen state to loading and then launches a coroutine on the main thread.
     *
     * It retrieves the original list of products and half-products from the dishDomain.
     * If the dishDomain is null, it defaults to an empty list.
     *
     * It then determines which products and half-products have been removed by filtering out items
     * that are in the original list but not in the current list of products and half-products.
     * These removed items are then mapped to their respective data model representations.
     *
     * Similarly, it determines which products and half-products have been edited by filtering out items
     * that are in the current list but not in the original list. These edited items are also mapped to their respective data model representations.
     *
     * The function then enters a try-catch block where it performs the following operations in the IO context:
     * - It iterates over the list of removed products and half-products and deletes each one from the repository.
     * - It iterates over the list of edited products and half-products and updates each one in the repository.
     * - It updates the dish in the repository. If the dishDomain is null, it throws an exception.
     *
     * If all operations are successful, it sets the screen state to success. If an exception is caught, it sets the screen state to error.
     */
    fun saveDish() {
        val dish = dish.value ?: return
        _screenState.value = ScreenState.Loading<Nothing>()
        viewModelScope.launch(Dispatchers.Default) {

            val editedProducts =
                dish.products.filterNot { it in originalProducts }.map { it.toProductDish() }
            val editedHalfProducts = dish.halfProducts.filterNot { it in originalHalfProducts }
                .map { it.toHalfProductDish() }

            val removedProducts = originalProducts.filterNot {
                it.id in dish.products.map { product -> product.id }
            }.map { it.toProductDish() }

            val removedHalfProducts = originalHalfProducts.filterNot {
                it.id in dish.halfProducts.map { halfProduct -> halfProduct.id }
            }.map { it.toHalfProductDish() }

            try {
                withContext(Dispatchers.IO) {

                    removedProducts.forEach { dishRepository.deleteProductDish(it) }
                    removedHalfProducts.forEach { dishRepository.deleteHalfProductDish(it) }

                    editedProducts.forEach { dishRepository.updateProductDish(it) }
                    editedHalfProducts.forEach { dishRepository.updateHalfProductDish(it) }

                    dishRepository.updateDish(this@DishDetailsViewModel.dish.value!!.toDishBase()) // Throw and handle if dishDomain is null
                }
                _screenState.value = ScreenState.Success<Nothing>()

                // Clear pending navigation after successful save
                pendingNavigation = null
            } catch (e: Exception) {
                _screenState.value = ScreenState.Error(Error(e.message))
            }
        }
    }

    fun shareDish(context: Context) {
        analyticsRepository.logEvent(Constants.Analytics.DISH_SHARE, Bundle().apply{
            putString(Constants.Analytics.DISH_NAME, _dish.value?.name)
        })

        val shareableText = _dish.value?.toShareableText(context, currency.value).also {
            Timber.i(it)
        }
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareableText)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        context.startActivity(shareIntent)
    }

    fun onDeleteDishClick() {
        val dish = _dish.value ?: return
        analyticsRepository.logEvent(Constants.Analytics.DishV2.DELETE, null)
        _screenState.update {
            ScreenState.Interaction(
                InteractionType.DeleteConfirmation(dish.id, dish.name)
            )
        }
    }

    fun confirmDelete(dishId: Long) {
        _screenState.value = ScreenState.Loading<Nothing>()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                dishRepository.deleteDish(dishId)
                analyticsRepository.logEvent(Constants.Analytics.DishV2.DELETED, null)
                _screenState.value = ScreenState.Success<Nothing>()
            } catch (e: Exception) {
                _screenState.value = ScreenState.Error(Error(e.message))
            }
        }
    }

    fun toggleRecipeViewMode() = recipeHandler.toggleRecipeViewMode()
    fun cancelRecipeEdit() = recipeHandler.cancelRecipeEdit(_dish.value?.recipe)
    fun onChangeServings() = recipeHandler.onChangeServings()
    fun updateServings(servings: String) = recipeHandler.updateServings(servings)
    fun saveRecipe() = recipeHandler.saveRecipe(_dish.value)
}