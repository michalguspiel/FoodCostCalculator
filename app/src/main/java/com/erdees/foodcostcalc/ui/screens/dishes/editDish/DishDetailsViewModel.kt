package com.erdees.foodcostcalc.ui.screens.dishes.editDish

import android.content.Context
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
import com.erdees.foodcostcalc.domain.model.dish.DishDetailsActionResultType
import com.erdees.foodcostcalc.domain.model.dish.DishDomain
import com.erdees.foodcostcalc.domain.model.halfProduct.UsedHalfProductDomain
import com.erdees.foodcostcalc.domain.model.product.UsedProductDomain
import com.erdees.foodcostcalc.domain.usecase.CopyDishUseCase
import com.erdees.foodcostcalc.domain.usecase.DeleteDishUseCase
import com.erdees.foodcostcalc.domain.usecase.SaveDishUseCase
import com.erdees.foodcostcalc.domain.usecase.ShareDishUseCase
import com.erdees.foodcostcalc.ui.navigation.FCCScreen.Companion.DISH_ID_KEY
import com.erdees.foodcostcalc.ui.navigation.FCCScreen.Companion.IS_COPIED
import com.erdees.foodcostcalc.ui.screens.recipe.RecipeHandler
import com.erdees.foodcostcalc.ui.screens.recipe.RecipeUpdater
import com.erdees.foodcostcalc.ui.screens.recipe.RecipeViewMode
import com.erdees.foodcostcalc.utils.Constants
import com.erdees.foodcostcalc.utils.MyDispatchers
import com.erdees.foodcostcalc.utils.UnsavedChangesValidator
import com.erdees.foodcostcalc.utils.onNumericValueChange
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val deleteDishUseCase: DeleteDishUseCase by inject()
    private val shareDishUseCase: ShareDishUseCase by inject()
    private val dishRepository: DishRepository by inject()
    private val preferences: Preferences by inject()
    private val analyticsRepository: AnalyticsRepository by inject()
    private val myDispatchers: MyDispatchers by inject()

    private val _uiState = MutableStateFlow(DishDetailsUiState())
    val uiState = _uiState.asStateFlow()

    private var originalDish: DishDomain? = null
    private var originalProducts: List<UsedProductDomain> = listOf()
    private var originalHalfProducts: List<UsedHalfProductDomain> = listOf()

    private val recipeHandler: RecipeHandler = RecipeHandler(
        viewModelScope = viewModelScope,
        resetScreenState = { resetScreenState() },
        updateScreenState = { screenState -> _uiState.update { it.copy(screenState = screenState) } },
        updateDish = { dish -> _uiState.update { it.copy(dish = dish) } }
    )

    private val interactionHandler = InteractionHandler()
    private val dishPropertySaver = DishPropertySaver()

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

    val screenState: StateFlow<ScreenState> = uiState.map { it.screenState }
        .stateIn(viewModelScope, SharingStarted.Eagerly, ScreenState.Idle)
    val dish: StateFlow<DishDomain?> =
        uiState.map { it.dish }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    init {
        viewModelScope.launch {
            preferences.currency.collect { currency ->
                _uiState.update { it.copy(currency = currency) }
            }
        }
        fetchDishAndUpdateScreenState()
        checkIfCopied()
    }

    private fun checkIfCopied() {
        val isCopied = savedStateHandle.get<Boolean>(IS_COPIED) ?: false
        if (isCopied) {
            _uiState.update { it.copy(showCopyConfirmation = true) }
        }
    }

    fun hideCopyConfirmation() {
        _uiState.update { it.copy(showCopyConfirmation = false) }
    }

    private fun fetchDishAndUpdateScreenState() {
        Timber.i("fetchDishAndUpdateScreenState()")
        _uiState.update { it.copy(screenState = ScreenState.Loading<Nothing>()) }
        viewModelScope.launch {
            try {
                loadDishStateFromRepository()
                _uiState.update { it.copy(screenState = ScreenState.Idle) }
            } catch (e: Exception) {
                _uiState.update { it.copy(screenState = ScreenState.Error(Error(e))) }
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

            _uiState.update {
                it.copy(
                    dish = this,
                    editableFields = it.editableFields.copy(
                        name = this.name
                    )
                )
            }

            originalDish = this.copy()
            recipeHandler.updateRecipe(this.recipe.toEditableRecipe())
            originalProducts = this.products
            originalHalfProducts = this.halfProducts
        }
    }

    fun updateName(value: String) {
        _uiState.update { it.copy(editableFields = it.editableFields.copy(name = value)) }
    }

    fun updateCopiedDishName(value: String) {
        _uiState.update { it.copy(editableFields = it.editableFields.copy(copiedDishName = value)) }
    }

    fun updateQuantity(value: String) {
        val sanitizedValue = onNumericValueChange(_uiState.value.editableFields.quantity, value)
        _uiState.update { it.copy(editableFields = it.editableFields.copy(quantity = sanitizedValue)) }
    }

    fun updateTax(value: String) {
        val sanitizedValue = onNumericValueChange(_uiState.value.editableFields.tax, value)
        _uiState.update { it.copy(editableFields = it.editableFields.copy(tax = sanitizedValue)) }
    }

    fun updateMargin(value: String) {
        val sanitizedValue = onNumericValueChange(_uiState.value.editableFields.margin, value)
        _uiState.update { it.copy(editableFields = it.editableFields.copy(margin = sanitizedValue)) }
    }

    fun updateTotalPrice(value: String) {
        val sanitizedValue = onNumericValueChange(_uiState.value.editableFields.totalPrice, value)
        _uiState.update { it.copy(editableFields = it.editableFields.copy(totalPrice = sanitizedValue)) }
    }

    fun setInteraction(interaction: InteractionType) {
        interactionHandler.handleInteraction(interaction, _uiState.value) { newState ->
            _uiState.update { newState }
        }
    }

    fun resetScreenState() {
        _uiState.update { it.copy(screenState = ScreenState.Idle) }
    }

    fun updateItemQuantity() {
        val value = _uiState.value.editableFields.quantity.toDoubleOrNull()
        val item = _uiState.value.currentlyEditedItem
        val currentDish = _uiState.value.dish

        if (value == null || item == null || currentDish == null) {
            return
        }

        when (item) {
            is UsedProductDomain -> {
                val index = currentDish.products.indexOf(item)
                if (index != -1) {
                    val updatedItem = item.copy(quantity = value)
                    _uiState.update {
                        it.copy(
                            dish = it.dish?.copy(
                                products = currentDish.products.toMutableList()
                                    .apply { set(index, updatedItem) }
                            )
                        )
                    }
                }
            }

            is UsedHalfProductDomain -> {
                val index = currentDish.halfProducts.indexOf(item)
                if (index != -1) {
                    val updatedItem = item.copy(quantity = value)
                    _uiState.update {
                        it.copy(
                            dish = it.dish?.copy(
                                halfProducts = currentDish.halfProducts.toMutableList()
                                    .apply { set(index, updatedItem) }
                            )
                        )
                    }
                }
            }
        }
        resetScreenState()
    }

    fun saveDishTax() {
        dishPropertySaver.saveProperty(
            propertyType = DishPropertySaver.PropertyType.TAX,
            uiState = _uiState.value,
            updateUiState = { newState -> _uiState.update { newState } }
        )
    }

    fun saveDishMargin() {
        dishPropertySaver.saveProperty(
            propertyType = DishPropertySaver.PropertyType.MARGIN,
            uiState = _uiState.value,
            updateUiState = { newState -> _uiState.update { newState } }
        )
    }

    fun saveDishTotalPrice() {
        dishPropertySaver.saveProperty(
            propertyType = DishPropertySaver.PropertyType.TOTAL_PRICE,
            uiState = _uiState.value,
            updateUiState = { newState -> _uiState.update { newState } }
        )
    }

    fun saveDishName() {
        dishPropertySaver.saveProperty(
            propertyType = DishPropertySaver.PropertyType.NAME,
            uiState = _uiState.value,
            updateUiState = { newState -> _uiState.update { newState } }
        )
    }

    /**
     * Removes item from the temporary list of items. Requires saving to persist.
     *
     * @param item The item to remove.
     * */
    fun removeItem(item: UsedItem) {
        Timber.i("removeItem: $item")
        val currentDish = _uiState.value.dish ?: return

        when (item) {
            is UsedProductDomain ->
                _uiState.update {
                    it.copy(
                        dish = currentDish.copy(
                            products = currentDish.products.filter { product -> product != item }
                        )
                    )
                }

            is UsedHalfProductDomain ->
                _uiState.update {
                    it.copy(
                        dish = currentDish.copy(
                            halfProducts = currentDish.halfProducts.filter { halfProduct -> halfProduct != item }
                        )
                    )
                }
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

        if (UnsavedChangesValidator.hasUnsavedChanges(originalDish, _uiState.value.dish)) {
            return true
        }

        // Deep check for products and half-products changes
        val productsChanged = originalProducts != _uiState.value.dish?.products
        val halfProductsChanged = originalHalfProducts != _uiState.value.dish?.halfProducts

        return productsChanged || halfProductsChanged
    }

    /**
     * Handles back navigation with unsaved changes check
     *
     * @param navigate The navigation action to perform if confirmed or no unsaved changes
     */
    fun handleBackNavigation(navigate: () -> Unit) {
        if (hasUnsavedChanges()) {
            _uiState.update {
                it.copy(
                    screenState = ScreenState.Interaction(InteractionType.UnsavedChangesConfirmation)
                )
            }
        } else {
            navigate()
        }
    }

    fun discardChangesAndProceed(getName: (String?) -> String) {
        // Restore dish to original state
        viewModelScope.launch {
            loadDishStateFromRepository()
            resetScreenState()
            showCopyDish(getName(_uiState.value.dish?.name))
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
            _uiState.update {
                it.copy(
                    screenState = ScreenState.Interaction(InteractionType.UnsavedChangesConfirmationBeforeCopy)
                )
            }
        } else {
            showCopyDish(getName(_uiState.value.dish?.name))
        }
    }

    fun saveDish(actionResultType: DishDetailsActionResultType = DishDetailsActionResultType.UPDATED_NAVIGATE) {
        val currentDish = _uiState.value.dish ?: return
        _uiState.update { it.copy(screenState = ScreenState.Loading<Nothing>()) }

        viewModelScope.launch {
            saveDishUseCase.invoke(
                dish = currentDish,
                originalProducts = originalProducts,
                originalHalfProducts = originalHalfProducts,
                actionResultType = actionResultType
            ).onSuccess { result ->
                if (actionResultType == DishDetailsActionResultType.UPDATED_STAY) {
                    loadDishStateFromRepository()
                }
                _uiState.update { it.copy(screenState = ScreenState.Success(result)) }
            }.onFailure { exception ->
                _uiState.update { it.copy(screenState = ScreenState.Error(Error(exception.message))) }
                Timber.e(exception, "Failed to save dish")
            }
        }
    }

    /**
     * Shares dish information using the Android share functionality.
     * Delegates to ShareDishUseCase, which handles the business logic.
     *
     * @param context Android context needed to start the share intent
     */
    fun shareDish(context: Context) {
        val currentDish = _uiState.value.dish ?: return

        viewModelScope.launch {
            shareDishUseCase.invoke(
                context = context,
                dish = currentDish,
                currency = _uiState.value.currency
            ).onSuccess { shareIntent ->
                context.startActivity(shareIntent)
            }.onFailure { exception ->
                _uiState.update { it.copy(screenState = ScreenState.Error(Error(exception.message))) }
                Timber.e(exception, "Failed to share dish")
            }
        }
    }

    fun onDeleteDishClick() {
        val currentDish = _uiState.value.dish ?: return
        analyticsRepository.logEvent(Constants.Analytics.DishV2.DELETE, null)
        _uiState.update {
            it.copy(
                screenState = ScreenState.Interaction(
                    InteractionType.DeleteConfirmation(currentDish.id, currentDish.name)
                )
            )
        }
    }

    fun confirmDelete(dishId: Long) {
        _uiState.update { it.copy(screenState = ScreenState.Loading<Nothing>()) }

        viewModelScope.launch {
            deleteDishUseCase.invoke(dishId).onSuccess { result ->
                _uiState.update { it.copy(screenState = ScreenState.Success(result)) }
            }.onFailure { exception ->
                _uiState.update { it.copy(screenState = ScreenState.Error(Error(exception.message))) }
                Timber.e(exception, "Failed to delete dish")
            }
        }
    }

    fun toggleRecipeViewMode() = recipeHandler.toggleRecipeViewMode()
    fun cancelRecipeEdit() = recipeHandler.cancelRecipeEdit(_uiState.value.dish?.recipe)
    fun onChangeServings() = recipeHandler.onChangeServings()
    fun updateServings(servings: String) = recipeHandler.updateServings(servings)
    fun saveRecipe() = recipeHandler.saveRecipe(_uiState.value.dish)

    /**
     * Creates a copy of the current dish with the name set in editableName.
     * On success, emits a ScreenState.Success with DishActionResult.COPIED and the new dish ID.
     */
    fun copyDish() {
        val currentDish = _uiState.value.dish ?: return
        val newName = _uiState.value.editableFields.copiedDishName

        _uiState.update { it.copy(screenState = ScreenState.Loading<Nothing>()) }

        viewModelScope.launch {
            copyDishUseCase.invoke(currentDish, newName).onSuccess { result ->
                _uiState.update { it.copy(screenState = ScreenState.Success(result)) }
            }.onFailure { error ->
                Timber.e("Failed to copy dish: ${error.message}", error)
                _uiState.update { it.copy(screenState = ScreenState.Error(Error(error.message))) }
            }
        }
    }

    /**
     * Called when user clicks "Discard" in the unsaved changes dialog
     * @param navigate The navigation action to perform after discarding
     */
    fun discardChanges(navigate: () -> Unit) {
        // Restore original dish state
        _uiState.update {
            it.copy(
                dish = originalDish?.copy(),
                editableFields = it.editableFields.copy(
                    name = originalDish?.name ?: ""
                )
            )
        }
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
