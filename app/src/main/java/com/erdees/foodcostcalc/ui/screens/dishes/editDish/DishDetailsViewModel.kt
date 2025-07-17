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
import com.erdees.foodcostcalc.domain.mapper.Mapper.toDishDomain
import com.erdees.foodcostcalc.domain.mapper.Mapper.toEditableRecipe
import com.erdees.foodcostcalc.domain.model.InteractionType
import com.erdees.foodcostcalc.domain.model.ScreenState
import com.erdees.foodcostcalc.domain.model.UsedItem
import com.erdees.foodcostcalc.domain.model.dish.DishActionResult
import com.erdees.foodcostcalc.domain.model.dish.DishDetailsActionResultType
import com.erdees.foodcostcalc.domain.model.dish.DishDomain
import com.erdees.foodcostcalc.domain.model.halfProduct.UsedHalfProductDomain
import com.erdees.foodcostcalc.domain.model.product.UsedProductDomain
import com.erdees.foodcostcalc.domain.usecase.CopyDishUseCase
import com.erdees.foodcostcalc.domain.usecase.SaveDishUseCase
import com.erdees.foodcostcalc.ext.toShareableText
import com.erdees.foodcostcalc.ui.navigation.FCCScreen.Companion.DISH_ID_KEY
import com.erdees.foodcostcalc.ui.navigation.FCCScreen.Companion.IS_COPIED
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
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

/**
 * Shared ViewModel between [DishDetailsScreen] and [RecipeScreen].
 * It was decided to share it in order to avoid passing data between screens.
 * */
class DishDetailsViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel(),
    KoinComponent {

    private val copyDishUseCase: CopyDishUseCase by inject()
    private val saveDishUseCase: SaveDishUseCase by inject()
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

    private var _editableCopiedDishName: MutableStateFlow<String> = MutableStateFlow("")
    val editableCopiedDishName: StateFlow<String> = _editableCopiedDishName

    private var _editableTotalPrice: MutableStateFlow<String> = MutableStateFlow("")
    val editableTotalPrice: StateFlow<String> = _editableTotalPrice

    private var originalDish: DishDomain? = null

    val currency = preferences.currency.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val recipeHandler: RecipeHandler = RecipeHandler(
        viewModelScope = viewModelScope,
        resetScreenState = { resetScreenState() },
        updateScreenState = { _screenState.value = it },
        updateDish = { _dish.value = it })

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

    private val _showCopyConfirmation = MutableStateFlow(false)
    val showCopyConfirmation: StateFlow<Boolean> = _showCopyConfirmation

    init {
        fetchDishAndUpdateScreenState()
        checkIfCopied()
    }

    private fun checkIfCopied() {
        val isCopied = savedStateHandle.get<Boolean>(IS_COPIED) ?: false
        if (isCopied) {
            _showCopyConfirmation.value = true
        }
    }

    fun hideCopyConfirmation() {
        _showCopyConfirmation.value = false
    }

    private fun fetchDishAndUpdateScreenState() {
        Timber.i("fetchDishAndUpdateScreenState()")
        _screenState.update { ScreenState.Loading<Nothing>() }
        viewModelScope.launch {
            try {
                loadDishStateFromRepository()
                _screenState.update { ScreenState.Idle }
            } catch (e: Exception) {
                _screenState.update { ScreenState.Error(Error(e)) }
            }
        }
    }

    private suspend fun loadDishStateFromRepository() {
        Timber.i(
            "loadDishStateFromRepository \n SavedStateHandle: $savedStateHandle, ${
                savedStateHandle.get<Long>(
                    "dishId"
                )
            }"
        )
        val id = savedStateHandle.get<Long>(DISH_ID_KEY)
            ?: throw NullPointerException("Failed to fetch dish due to missing id in savedStateHandle")
        val dish = dishRepository.getDish(id).flowOn(myDispatchers.ioDispatcher).first()
        with(dish.toDishDomain()) {
            Timber.i("Fetched dish: $this")
            if (this.recipe == null) {
                recipeHandler.updateRecipeViewMode(RecipeViewMode.EDIT)
            }
            _dish.update { this }
            _editableName.update { this.name }
            originalDish = this.copy()
            recipeHandler.updateRecipe(this.recipe.toEditableRecipe())
            originalProducts = this.products
            originalHalfProducts = this.halfProducts
        }
    }

    fun updateName(value: String) {
        _editableName.value = value
    }

    fun updateCopiedDishName(value: String) {
        _editableCopiedDishName.value = value
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
                val price = Utils.formatPriceWithoutSymbol(
                    dish.value?.totalPrice, currency.value?.currencyCode
                )
                _editableTotalPrice.value = price
            }

            is InteractionType.CopyDish ->
                _editableCopiedDishName.value = interaction.prefilledName

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
                    _dish.value = _dish.value?.copy(
                        halfProducts = dish.halfProducts.toMutableList()
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
            _screenState.value = ScreenState.Error(Error("Invalid total price format."))
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
        Timber.i("removeItem: $item")
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

        if (UnsavedChangesValidator.hasUnsavedChanges(originalDish, _dish.value)) {
            return true
        }

        // Deep check for products and half-products changes
        val productsChanged = originalProducts != _dish.value?.products
        val halfProductsChanged = originalHalfProducts != _dish.value?.halfProducts

        return productsChanged || halfProductsChanged
    }

    /**
     * Handles back navigation with unsaved changes check
     *
     * @param navigate The navigation action to perform if confirmed or no unsaved changes
     */
    fun handleBackNavigation(navigate: () -> Unit) {
        if (hasUnsavedChanges()) {
            _screenState.update { ScreenState.Interaction(InteractionType.UnsavedChangesConfirmation) }
        } else {
            navigate()
        }
    }

    fun discardChangesAndProceed(getName: (String?) -> String) {
        // Restore dish to original state
        viewModelScope.launch {
            loadDishStateFromRepository()
            resetScreenState()
            showCopyDish(getName(_dish.value?.name))
        }
    }

    private fun showCopyDish(prefilledName: String) {
        setInteraction(InteractionType.CopyDish(prefilledName))
    }

    fun saveChangesAndProceed() {
        saveDish(DishDetailsActionResultType.UPDATED_STAY)
    }

    /**
     * Handles the "Copy Dish" action with unsaved changes check
     * If there are unsaved changes, prompts the user to decide what to do first
     * Otherwise, proceeds directly to the copy dish dialog
     */
    fun handleCopyDish(getName: (String?) -> String) {
        if (hasUnsavedChanges()) {
            _screenState.update {
                ScreenState.Interaction(InteractionType.UnsavedChangesConfirmationBeforeCopy)
            }
        } else {
            showCopyDish(getName(_dish.value?.name))
        }
    }

    /**
     * Saves the current dish state by delegating to SaveDishUseCase.
     * It handles loading state, executes the use case, and updates screen state based on the result.
     *
     * @param actionResultType The type of action to perform after saving (navigate or stay)
     */
    fun saveDish(actionResultType: DishDetailsActionResultType = DishDetailsActionResultType.UPDATED_NAVIGATE) {
        val dish = dish.value ?: return
        _screenState.value = ScreenState.Loading<Nothing>()

        viewModelScope.launch {
            saveDishUseCase.invoke(
                dish = dish,
                originalProducts = originalProducts,
                originalHalfProducts = originalHalfProducts,
                actionResultType = actionResultType
            ).onSuccess { result ->
                if (actionResultType == DishDetailsActionResultType.UPDATED_STAY) {
                    loadDishStateFromRepository()
                }
                _screenState.value = ScreenState.Success(result)
            }.onFailure { exception ->
                _screenState.value = ScreenState.Error(Error(exception.message))
                Timber.e(exception, "Failed to save dish")
            }
        }
    }

    fun shareDish(context: Context) {
        analyticsRepository.logEvent(Constants.Analytics.DISH_SHARE, Bundle().apply {
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
                _screenState.value =
                    ScreenState.Success(
                        DishActionResult(DishDetailsActionResultType.DELETED, dishId)
                    )
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

    /**
     * Creates a copy of the current dish with the name set in editableName.
     * On success, emits a ScreenState.Success with DishActionResult.COPIED and the new dish ID.
     */
    fun copyDish() {
        val currentDish = dish.value ?: return
        val newName = editableCopiedDishName.value

        _screenState.value = ScreenState.Loading<Nothing>()

        viewModelScope.launch {
            copyDishUseCase.invoke(currentDish, newName).onSuccess { result ->
                _screenState.value = ScreenState.Success(result)
            }.onFailure {
                Timber.e("Failed to copy dish: ${it.message}", it)
                _screenState.value = ScreenState.Error(Error(it.message))
            }
        }
    }

    /**
     * Called when user clicks "Discard" in the unsaved changes dialog
     * @param navigate The navigation action to perform after discarding
     */
    fun discardChanges(navigate: () -> Unit) {
        // Restore original dish state
        _dish.value = originalDish?.copy()
        recipeHandler.updateRecipe(originalDish?.recipe.toEditableRecipe())
        resetScreenState()
        // Navigate back
        navigate()
    }

    /**
     * Called when user clicks "Save" in the unsaved changes dialog
     * with navigation intent
     */
    fun saveAndNavigate() {
        // Set the pending intent to navigate back after saving
        // Save dish will trigger the success state, which will then check pendingIntent
        saveDish(DishDetailsActionResultType.UPDATED_NAVIGATE)
    }
}