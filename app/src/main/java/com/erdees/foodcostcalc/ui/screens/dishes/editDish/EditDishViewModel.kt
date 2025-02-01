package com.erdees.foodcostcalc.ui.screens.dishes.editDish

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.model.Recipe
import com.erdees.foodcostcalc.data.model.RecipeStep
import com.erdees.foodcostcalc.data.repository.DishRepository
import com.erdees.foodcostcalc.data.repository.RecipeRepository
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
import com.erdees.foodcostcalc.domain.model.recipe.EditableRecipe
import com.erdees.foodcostcalc.ui.navigation.FCCScreen.Companion.DISH_ID_KEY
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
 * Wrapper to ease passing down functions to composable.
 * */
data class RecipeUpdater(
    val updatePrepTime: (String) -> Unit,
    val updateCookTime: (String) -> Unit,
    val updateDescription: (String) -> Unit,
    val updateTips: (String) -> Unit,
    val updateStep: (Int, String) -> Unit
)

enum class RecipeViewMode {
    VIEW, EDIT
}

class EditDishViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel(),
    KoinComponent {

    private val dishRepository: DishRepository by inject()
    private val recipeRepository: RecipeRepository by inject()

    private var _screenState: MutableStateFlow<ScreenState> = MutableStateFlow(ScreenState.Idle)
    val screenState: StateFlow<ScreenState> = _screenState

    private var _recipeViewModeState: MutableStateFlow<RecipeViewMode> =
        MutableStateFlow(RecipeViewMode.EDIT)
    val recipeViewModeState: StateFlow<RecipeViewMode> = _recipeViewModeState

    private var _dish = MutableStateFlow<DishDomain?>(null)
    val dish: StateFlow<DishDomain?> = _dish

    /**
     * In order to have it persisted we must save it to the DB. This one is temporary.
     * */
    private var _recipe = MutableStateFlow<EditableRecipe>(EditableRecipe())
    val recipe: StateFlow<EditableRecipe> = _recipe

    fun updatePrepTime(prepTime: String) {
        prepTime.toIntOrNull() ?: return
        _recipe.update {
            _recipe.value.copy(prepTimeMinutes = prepTime)
        }
    }

    fun updateCookTime(cookTime: String) {
        cookTime.toIntOrNull() ?: return
        _recipe.update {
            _recipe.value.copy(cookTimeMinutes = cookTime)
        }
    }

    fun updateDescription(description: String) {
        _recipe.update {
            _recipe.value.copy(description = description)
        }
    }

    fun updateTips(tips: String) {
        _recipe.update {
            _recipe.value.copy(tips = tips)
        }
    }

    fun updateStep(index: Int, newStep: String) {
        _recipe.update {
            val updatedSteps = _recipe.value.steps.toMutableList()

            if (index in updatedSteps.indices) {
                updatedSteps[index] = newStep // Edit existing step
            } else {
                updatedSteps.add(newStep) // Add new step
            }

            _recipe.value.copy(steps = updatedSteps)
        }
    }


    private var _editableName: MutableStateFlow<String> = MutableStateFlow("")
    val editableName: StateFlow<String> = _editableName

    init {
        Timber.i("initialize \n SavedStateHandle: $savedStateHandle, ${savedStateHandle.get<Long>("dishId")}")
        _screenState.update { ScreenState.Loading() }
        viewModelScope.launch {
            try {
                val id = savedStateHandle.get<Long>(DISH_ID_KEY) ?: throw NullPointerException()
                val dish = dishRepository.getDish(id).flowOn(Dispatchers.IO).first()
                with(dish.toDishDomain()) {
                    if (this.recipe == null) {
                        _recipeViewModeState.update { RecipeViewMode.EDIT }
                    }
                    _dish.update { this }
                    _recipe.update { this.recipe.toEditableRecipe() }
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

            else -> {}
        }
        _screenState.value = ScreenState.Interaction(interaction)
    }

    fun resetScreenState() {
        _screenState.value = ScreenState.Idle
    }


    fun updateItemQuantity() {
        val value = editableQuantity.value.toDoubleOrNull() ?: return
        val item = currentlyEditedItem.value ?: return
        val dish = dish.value ?: return
        when (item) {
            is UsedProductDomain -> {
                val index = dish.products.indexOf(item)
                if (index != -1) {
                    val updatedItem = item.copy(quantity = value)
                    _dish.value = _dish.value?.copy(products = dish.products.toMutableList()
                        .apply { set(index, updatedItem) })
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

    fun deleteDish(dishId: Long) {
        _screenState.value = ScreenState.Loading()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                dishRepository.deleteDish(dishId)
                _screenState.value = ScreenState.Success()
            } catch (e: Exception) {
                _screenState.value = ScreenState.Error(Error(e.message))
            }
        }
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
        _screenState.value = ScreenState.Loading()
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

                    dishRepository.updateDish(this@EditDishViewModel.dish.value!!.toDishBase()) // Throw and handle if dishDomain is null
                }
                _screenState.value = ScreenState.Success()
            } catch (e: Exception) {
                _screenState.value = ScreenState.Error(Error(e.message))
            }
        }
    }

    fun toggleRecipeViewMode() {
        if (_recipeViewModeState.value == RecipeViewMode.VIEW) {
            _recipeViewModeState.update { RecipeViewMode.EDIT }
        } else {
            _recipeViewModeState.update { RecipeViewMode.VIEW }
        }
    }

    fun cancelEdit() {
        //todo
    }

    fun saveRecipe() {
        val recipeDomain = _recipe.value
        _screenState.value = ScreenState.Loading()
        viewModelScope.launch(Dispatchers.Default) {
            runCatching {
                withContext(Dispatchers.IO) {
                    val recipe = Recipe(
                        recipeId = 0,
                        cookTimeMinutes = recipeDomain.cookTimeMinutes.toInt(),
                        prepTimeMinutes = recipeDomain.prepTimeMinutes.toInt(),
                        description = recipeDomain.description,
                        tips = recipeDomain.tips
                    )
                    val recipeId = recipeRepository.addRecipe(recipe)
                    Timber.i("Recipe saved with id: $recipeId \n $recipe")
                    val steps = recipeDomain.steps.mapIndexed { index, step ->
                        RecipeStep(
                            id = 0,
                            order = index,
                            recipeId = recipeId,
                            stepDescription = step
                        )
                    }
                    recipeRepository.addRecipeSteps(steps)
                    Timber.i("Steps saved: \n $steps")
                }
            }.onSuccess {
                _screenState.value = ScreenState.Success()
                Timber.i("saveRecipe Success")
            }.onFailure {
                _screenState.value = ScreenState.Error(Error(it.message))
            }
        }
    }
}