package com.erdees.foodcostcalc.ui.screens.dishes.createDishV2

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.data.Preferences
import com.erdees.foodcostcalc.data.model.local.DishBase
import com.erdees.foodcostcalc.data.repository.AnalyticsRepository
import com.erdees.foodcostcalc.data.repository.DishRepository
import com.erdees.foodcostcalc.data.repository.HalfProductRepository
import com.erdees.foodcostcalc.data.repository.ProductRepository
import com.erdees.foodcostcalc.domain.mapper.Mapper.toHalfProductDish
import com.erdees.foodcostcalc.domain.mapper.Mapper.toProductDish
import com.erdees.foodcostcalc.domain.model.InteractionType
import com.erdees.foodcostcalc.domain.model.ItemUsageEntry
import com.erdees.foodcostcalc.domain.model.ScreenState
import com.erdees.foodcostcalc.domain.model.halfProduct.HalfProductAddedToDish
import com.erdees.foodcostcalc.domain.model.halfProduct.HalfProductDomain
import com.erdees.foodcostcalc.domain.model.onboarding.OnboardingState
import com.erdees.foodcostcalc.domain.model.product.ProductAddedToDish
import com.erdees.foodcostcalc.domain.model.product.ProductDomain
import com.erdees.foodcostcalc.domain.usecase.CreateProductUseCase
import com.erdees.foodcostcalc.ui.errors.InvalidMarginFormatException
import com.erdees.foodcostcalc.ui.errors.InvalidTaxFormatException
import com.erdees.foodcostcalc.ui.errors.UserReportableError
import com.erdees.foodcostcalc.ui.screens.dishes.DishAnalyticsHelper
import com.erdees.foodcostcalc.ui.screens.dishes.forms.componentlookup.ComponentSelection
import com.erdees.foodcostcalc.ui.screens.dishes.forms.existingcomponent.ExistingItemFormData
import com.erdees.foodcostcalc.ui.screens.dishes.forms.newcomponent.NewProductFormData
import com.erdees.foodcostcalc.utils.Constants
import com.erdees.foodcostcalc.utils.MyDispatchers
import com.erdees.foodcostcalc.utils.Utils
import com.erdees.foodcostcalc.utils.onNumericValueChange
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.SharingStarted.Companion.Lazily
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

/**
 * TODO: Refactor this VM to use ScreenState instead of separate state holders isLoading and errorRes
 * */
class CreateDishV2ViewModel : ViewModel(), KoinComponent {

    private val analyticsRepository: AnalyticsRepository by inject()
    private val productRepository: ProductRepository by inject()
    private val halfProductRepository: HalfProductRepository by inject()
    private val dishRepository: DishRepository by inject()
    private val preferences: Preferences by inject()
    private val dispatchers: MyDispatchers by inject()
    private val createProductUseCase: CreateProductUseCase by inject()
    private val analyticsHelper = DishAnalyticsHelper(analyticsRepository)

    private var _screenState: MutableStateFlow<ScreenState> = MutableStateFlow(ScreenState.Idle)
    val screenState: StateFlow<ScreenState> = _screenState

    private var _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val hasPromptedDefaultSettings = preferences.hasPromptedDefaultSettings
        .stateIn(viewModelScope, Eagerly, false)

    val onboardingState: StateFlow<OnboardingState?> =
        preferences.onboardingState.stateIn(viewModelScope, Eagerly, null)

    val isFirstDish: StateFlow<Boolean> = dishRepository.dishes.map {
        it.isEmpty()
    }.stateIn(viewModelScope, Eagerly, true)

    @StringRes
    private var _errorRes: MutableStateFlow<Int?> = MutableStateFlow(null)

    @StringRes
    val errorRes = _errorRes.asStateFlow()

    private var _saveDishSuccess: MutableStateFlow<Long?> = MutableStateFlow(null)

    /**
     * Emits the ID of the dish if it was successfully saved to the database, otherwise null.
     * */
    val saveDishSuccess = _saveDishSuccess.asStateFlow()

    val currency = preferences.currency.stateIn(viewModelScope, Lazily, null)

    private val _addedComponents: MutableStateFlow<List<ItemUsageEntry>> =
        MutableStateFlow(listOf())
    val addedComponents = _addedComponents.asStateFlow()

    private var _dishName = MutableStateFlow("")
    val dishName = _dishName

    fun updateDishName(newValue: String) {
        _dishName.value = newValue
    }

    private val _marginPercentInput: MutableStateFlow<String> = MutableStateFlow("")
    val marginPercentInput: StateFlow<String> = _marginPercentInput.onStart {
        _marginPercentInput.value = preferences.defaultMargin.first()
    }.stateIn(
        viewModelScope, Eagerly, ""
    )

    fun updateMarginPercentInput(newMargin: String) {
        onNumericValueChange(newMargin, _marginPercentInput)
    }

    private val _taxPercentInput = MutableStateFlow(preferences.defaultTax.toString())
    val taxPercentInput: StateFlow<String> = _taxPercentInput.onStart {
        _taxPercentInput.value = preferences.defaultTax.first()
    }.stateIn(
        viewModelScope, Eagerly, ""
    )

    fun updateTaxPercentInput(newTax: String) {
        onNumericValueChange(newTax, _taxPercentInput)
    }

    private var _componentSelection: MutableStateFlow<ComponentSelection?> = MutableStateFlow(null)
    val componentSelection = _componentSelection.asStateFlow()

    init {
        analyticsHelper.logFlowStarted()
    }

    val foodCost: StateFlow<Double> = _addedComponents.map { components ->
        components.sumOf { item ->
            item.foodCost
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    val finalSellingPrice: StateFlow<Double> = combine(
        foodCost, // Use the derived foodCost StateFlow
        marginPercentInput.filter { it.toDoubleOrNull() != null },
        taxPercentInput.filter { it.toDoubleOrNull() != null }
    ) { cost, marginStr, taxStr ->
        val margin = marginStr.toDouble()
        val tax = taxStr.toDouble()
        if (cost <= 0.0) {
            0.0
        } else {
            Utils.getDishFinalPrice(cost, margin, tax)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    /**
     * Called when user clicks add new ingredient in parent form
     * */
    fun onAddIngredientClick() {
        analyticsHelper.logAddIngredientClick(componentSelection.value)
        updateScreenState(ScreenState.Interaction(InteractionType.ContextualAddComponent))
    }

    fun setComponentSelection(componentSelection: ComponentSelection?) {
        _componentSelection.value = componentSelection
    }

    fun resetScreenState() {
        _screenState.update { ScreenState.Idle }
        _isLoading.update { false }
        _componentSelection.value = null
    }

    fun updateScreenState(screenState: ScreenState) {
        _screenState.value = screenState
    }

    /**
     * Called when the user submits the form to add a completely new product to the dish.
     * It coordinates saving the new product to the database and then adding it to the current dish's ingredient list.
     */
    fun onAddNewProduct(newProductFormData: NewProductFormData) {
        _isLoading.update { true }
        val newComponent = (_componentSelection.value as? ComponentSelection.NewComponent)
            ?: error("Component selection must be of type NewComponent to add a new product.")

        analyticsHelper.logNewProductSaveAttempt(newComponent.name)
        viewModelScope.launch(dispatchers.ioDispatcher) {
            try {
                createProductUseCase.invoke(newComponent.name, newProductFormData)
                    .onSuccess { newlyCreatedProduct ->
                        analyticsHelper.logNewProductSaveSuccess(newlyCreatedProduct)

                        addProductToDishList(
                            newlyCreatedProduct,
                            newProductFormData.quantityAddedToDish,
                            newProductFormData.unitForDish
                        )

                        resetProductAdditionState()
                    }
                    .onFailure { exception ->
                        analyticsHelper.logNewProductSaveFailure()
                        handleError(exception)
                    }
            } catch (e: Exception) {
                analyticsHelper.logNewProductSaveFailure()
                handleError(e)
            }
        }
    }

    /**
     * Called when the user confirms adding a pre-existing, selected component to the current dish.
     *
     * @param existingComponentFormData Data containing the quantity and unit for the component in the dish.
     */
    fun onAddExistingComponent(existingComponentFormData: ExistingItemFormData) {
        _isLoading.update { true }
        viewModelScope.launch {
            try {
                val componentSelection = _componentSelection.value as? ComponentSelection.ExistingComponent
                    ?: error("Component selection must be of type ExistingComponent to add existing component.")

                when (val item = componentSelection.item) {
                    is ProductDomain -> {
                        addProductToDishList(
                            product = item,
                            quantityStr = existingComponentFormData.quantityForDish,
                            unit = existingComponentFormData.unitForDish
                        )
                    }

                    is HalfProductDomain -> {
                        addHalfProductToDishList(
                            halfProduct = item,
                            quantityStr = existingComponentFormData.quantityForDish,
                            unit = existingComponentFormData.unitForDish
                        )
                    }

                    else -> {
                        error("Unsupported component type: ${item::class.simpleName}")
                    }
                }

                resetProductAdditionState()
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    /**
     * Adds a given product (either newly created or existing) to the list of products
     * currently added to the dish for UI display.
     *
     * @param product The ProductDomain object to add.
     * @param quantityStr The quantity of this product to add to the dish.
     * @param unit The unit for the quantity added to the dish.
     * @throws IllegalStateException if the quantity is missing or invalid.
     */
    private fun addProductToDishList(product: ProductDomain, quantityStr: String, unit: String) {
        val quantityAddedToDish = quantityStr.toDoubleOrNull()
            ?: error("Quantity for the product in the dish cannot be empty or invalid.")

        val productAddedToDish = ProductAddedToDish(
            item = product,
            quantity = quantityAddedToDish,
            quantityUnit = unit
        )
        _addedComponents.update { currentList ->
            currentList + productAddedToDish
        }
        analyticsHelper.logProductAddedToDishList(
            product.name,
            componentSelection.value,
            quantityAddedToDish,
            unit
        )
    }

    /**
     * Adds a given half product to the list of components currently added to the dish for UI display.
     *
     * @param halfProduct The HalfProductDomain object to add.
     * @param quantityStr The quantity of this half product to add to the dish.
     * @param unit The unit for the quantity added to the dish.
     * @throws IllegalStateException if the quantity is missing or invalid.
     */
    private fun addHalfProductToDishList(halfProduct: HalfProductDomain, quantityStr: String, unit: String) {
        val quantityAddedToDish = quantityStr.toDoubleOrNull()
            ?: error("Quantity for the half product in the dish cannot be empty or invalid.")

        val halfProductAddedToDish = HalfProductAddedToDish(
            item = halfProduct,
            quantity = quantityAddedToDish,
            quantityUnit = unit
        )
        Timber.i("Adding half product to dish list: $halfProductAddedToDish")
        _addedComponents.update { currentList ->
            currentList + halfProductAddedToDish
        }
        analyticsHelper.logHalfProductAddedToDishList()
    }

    /**
     * Resets the state related to adding a new product to the dish.
     * This function is typically called after a product (either new or existing)
     * has been successfully processed and added to the list of ingredients for the current dish.
     *
     * It performs the following actions:
     * - Sets the loading state (`_isLoading`) to `false`.
     * - Clears the input field for the new product name (`_newProductName`).
     * - Clears any selected product from suggestions (`selectedSuggestedProduct`).
     * - Ensures that product suggestions are hidden by calling `onSuggestionsManuallyDismissed()`.
     * - Dismisses any modal that might have been used for adding the product by calling `onModalDismissed()`.
     */
    private fun resetProductAdditionState() {
        _isLoading.update { false }
        _componentSelection.value = null
        resetScreenState()
    }

    /**
     * Handles the logic for saving the currently constructed dish and its ingredients to the database.
     * It first saves the main dish entity, then iterates through the added products
     * to save each as an ingredient linked to the dish.
     */
    private suspend fun onSaveDish() {
        resetScreenState()
        _isLoading.update { true }
        withContext(dispatchers.ioDispatcher) {
            try {
                analyticsHelper.logDishSaveAttempt(
                    dishName.value,
                    marginPercentInput.value,
                    taxPercentInput.value,
                    addedComponents.value.size
                )
                val dishId = saveDishBase()

                saveDishIngredients(dishId)
                saveDishHalfProducts(dishId)

                _isLoading.update { false }
                _saveDishSuccess.update { dishId }
                analyticsHelper.logDishSaveSuccess(
                    dishName.value,
                    addedComponents.value.size,
                    dishRepository.getDishCount()
                )
            } catch (e: Exception) {
                analyticsHelper.logDishSaveFailureAnalytics(dishName.value)
                handleError(e)
            }
        }
    }

    fun onSaveDishClick() {
        if (!hasPromptedDefaultSettings.value) {
            showSetAsDefaultSettingsPrompt()
        } else {
            viewModelScope.launch {
                onSaveDish()
            }
        }
    }

    /**
     * Creates a [DishBase] object from the current ViewModel state (dish name, margin, tax)
     * and saves it to the [dishRepository].
     *
     * @return The ID of the newly saved dish.
     * @throws NumberFormatException if margin or tax percentages are not valid numbers.
     */
    private suspend fun saveDishBase(): Long {
        val margin = marginPercentInput.value.toDoubleOrNull()
            ?: throw InvalidMarginFormatException(
                "Margin percentage is not a valid number: ${marginPercentInput.value}",
            )
        val tax = taxPercentInput.value.toDoubleOrNull()
            ?: throw InvalidTaxFormatException("Tax percentage is not a valid number: ${taxPercentInput.value}")

        val dish = DishBase(
            dishId = 0,
            name = dishName.value,
            marginPercent = margin,
            dishTax = tax,
            recipeId = null
        )
        return dishRepository.addDish(dish)
    }

    /**
     * Iterates through the list of products added to the dish ([_addedComponents])
     * and saves each one as a [ProductDish] entity linked to the given [dishId]
     * using the [productRepository].
     *
     * @param dishId The ID of the dish to which these ingredients belong.
     */
    private suspend fun saveDishIngredients(dishId: Long) {
        _addedComponents.value.filterIsInstance<ProductAddedToDish>().forEach { productAddedToDish ->
            productRepository.addProductDish(productAddedToDish.toProductDish(dishId))
        }
    }

    private suspend fun saveDishHalfProducts(dishId: Long) {
        _addedComponents.value.filterIsInstance<HalfProductAddedToDish>().forEach { halfProduct ->
            halfProductRepository.addHalfProductDish(halfProduct.toHalfProductDish(dishId))
        }
    }

    /**
     * Handles common error logic by updating the error StateFlow and resetting the loading state.
     *
     * @param throwable The exception/throwable that occurred.
     */
    private fun handleError(throwable: Throwable) {
        Timber.e(throwable)
        val errorResId =
            if (throwable is UserReportableError) throwable.errorRes else R.string.unexpected_error_occurred

        analyticsHelper.logHandleError(throwable, errorResId)
        _errorRes.update { errorResId }
    }

    /**
     * Called after successful dish creation to check if we should prompt the user
     * to save their margin and tax settings as defaults.
     */
    private fun showSetAsDefaultSettingsPrompt() {
        analyticsRepository.logEvent(Constants.Analytics.DishV2.DEFAULT_SETTINGS_PROMPT_SHOWN)
        updateScreenState(
            ScreenState.Interaction(
                InteractionType.SaveDefaultSettings(
                    margin = marginPercentInput.value,
                    tax = taxPercentInput.value
                )
            )
        )
    }

    /**
     * Saves the current margin and tax values as default settings and marks that we've prompted the user.
     */
    fun saveAsDefaultSettings() {
        viewModelScope.launch(dispatchers.ioDispatcher) {
            try {
                preferences.setDefaultMargin(marginPercentInput.value)
                preferences.setDefaultTax(taxPercentInput.value)
                preferences.setHasPromptedDefaultSettings(true)
                analyticsRepository.logEvent(
                    Constants.Analytics.DishV2.DEFAULT_SETTINGS_SAVED
                )
                onSaveDish()
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    /**
     * Dismisses the default settings prompt without saving the values.
     */
    fun dismissDefaultSettingsPrompt() {
        viewModelScope.launch(dispatchers.ioDispatcher) {
            preferences.setHasPromptedDefaultSettings(true)
            analyticsRepository.logEvent(Constants.Analytics.DishV2.DEFAULT_SETTINGS_DISMISSED)
            onSaveDish()
        }
    }

    fun dismissError() {
        _errorRes.value = null
    }

    /**
     * Resets the save dish success state to prevent re-navigation.
     * Call this after navigation has been handled.
     */
    fun resetSaveDishSuccess() {
        _saveDishSuccess.value = null
    }

    fun onboardingComplete() {
        viewModelScope.launch {
            preferences.setOnboardingState(OnboardingState.FINISHED)
        }
    }
}
