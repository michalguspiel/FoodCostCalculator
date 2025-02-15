package com.erdees.foodcostcalc.ui.screens.recipe

import com.erdees.foodcostcalc.data.repository.AnalyticsRepository
import com.erdees.foodcostcalc.data.repository.DishRepository
import com.erdees.foodcostcalc.data.repository.RecipeRepository
import com.erdees.foodcostcalc.domain.mapper.Mapper.toEditableRecipe
import com.erdees.foodcostcalc.domain.mapper.Mapper.toRecipe
import com.erdees.foodcostcalc.domain.mapper.Mapper.toRecipeDomain
import com.erdees.foodcostcalc.domain.mapper.Mapper.toRecipeStep
import com.erdees.foodcostcalc.domain.model.InteractionType
import com.erdees.foodcostcalc.domain.model.ScreenState
import com.erdees.foodcostcalc.domain.model.dish.DishDomain
import com.erdees.foodcostcalc.domain.model.recipe.EditableRecipe
import com.erdees.foodcostcalc.domain.model.recipe.RecipeDomain
import com.erdees.foodcostcalc.domain.model.recipe.RecipeStepDomain
import com.erdees.foodcostcalc.utils.Constants
import com.erdees.foodcostcalc.domain.model.errors.DishNotFound
import com.erdees.foodcostcalc.utils.onIntegerValueChange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

enum class RecipeViewMode {
    VIEW, EDIT
}

sealed class RecipeEvent {
    data object CancelEditRecipeMissing : RecipeEvent()
}

/**
 * The purpose of this class is to capture some of the logic from [DishDetailsViewModel],
 * so that it size can be reduced. The mentioned viewmodel handles recipe logic too,
 * since navigating to recipe can be only achieved from EditDishScreen.
 * By doing so there is no need for any object passing
 * */
class RecipeHandler(
    val viewModelScope: CoroutineScope,
    val resetScreenState: () -> Unit,
    val updateScreenState: (ScreenState) -> Unit,
    val updateDish: (DishDomain) -> Unit
) : KoinComponent {

    private val dishRepository: DishRepository by inject()
    private val recipeRepository: RecipeRepository by inject()
    private val analyticsRepository: AnalyticsRepository by inject()

    private var _recipeViewModeState: MutableStateFlow<RecipeViewMode> =
        MutableStateFlow(RecipeViewMode.VIEW)
    val recipeViewModeState: StateFlow<RecipeViewMode> = _recipeViewModeState

    private var _recipeServings: MutableStateFlow<Int> = MutableStateFlow(1)
    val recipeServings: StateFlow<Int> = _recipeServings

    private var _recipeEvent: Channel<RecipeEvent> = Channel()
    val recipeEvent = _recipeEvent.receiveAsFlow()

    fun updateRecipeViewMode(recipeViewMode: RecipeViewMode){
        _recipeViewModeState.update { recipeViewMode }
    }

    /**
     * In order to have it persisted we must save it to the DB. This one is temporary.
     * */
    private var _recipe = MutableStateFlow(EditableRecipe())
    val recipe: StateFlow<EditableRecipe> = _recipe

    fun updateRecipe(editableRecipe: EditableRecipe){
        _recipe.update { editableRecipe }
    }

    fun updateServings(servings: String) {
        servings.toIntOrNull()?.let {
            _recipeServings.value = it
        }
        resetScreenState()
    }

    fun onChangeServings() {
        analyticsRepository.logEvent(
            Constants.Analytics.Buttons.RECIPE_EDIT_DISPLAYED_PORTIONS,
            null
        )
        updateScreenState(ScreenState.Interaction(InteractionType.ChangeServings))
    }

    fun updatePrepTime(prepTime: String) {
        _recipe.update {
            _recipe.value.copy(
                prepTimeMinutes = onIntegerValueChange(
                    _recipe.value.prepTimeMinutes, prepTime
                )
            )
        }
    }

    fun updateCookTime(cookTime: String) {
        _recipe.update {
            _recipe.value.copy(
                cookTimeMinutes = onIntegerValueChange(
                    _recipe.value.cookTimeMinutes, cookTime
                )
            )
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
        Timber.i("updateStep: $index, newStep: $newStep")
        _recipe.update {
            val updatedSteps = _recipe.value.steps.toMutableList()
            Timber.i("updatedSteps: ${updatedSteps.map { it.id }}, indices: ${updatedSteps.indices}")
            if (index in updatedSteps.indices) {
                Timber.i("Edit existing step")
                updatedSteps[index] =
                    updatedSteps[index].copy(stepDescription = newStep)
            } else {
                Timber.i("Add new step")
                updatedSteps.add(RecipeStepDomain(null, index, newStep))
            }

            _recipe.value.copy(steps = updatedSteps)
        }
    }

    fun toggleRecipeViewMode() {
        if (_recipeViewModeState.value == RecipeViewMode.VIEW) {
            _recipeViewModeState.update { RecipeViewMode.EDIT }
        } else {
            _recipeViewModeState.update { RecipeViewMode.VIEW }
        }
        resetScreenState()
    }

    /**
     * Cancels editing of recipe, if dish does not include any recipe, sends event,
     * which in turn instructs screen to pop backstack.
     * */
    fun cancelRecipeEdit(currentRecipe: RecipeDomain?) {
        Timber.i("cancelRecipeEdit()")
        if (currentRecipe == null) {
            updateScreenState(ScreenState.Loading())
            viewModelScope.launch {
                _recipeEvent.trySend(RecipeEvent.CancelEditRecipeMissing)
            }
            updateScreenState(ScreenState.Idle)
        } else {
            Timber.i("dish recipe is $currentRecipe, toggling recipe view mode")
            _recipe.update { currentRecipe.toEditableRecipe() }
            toggleRecipeViewMode()
        }
    }

    fun saveRecipe(dish: DishDomain?) {
        Timber.i("saveRecipe()")
        updateScreenState(ScreenState.Loading())
        viewModelScope.launch(Dispatchers.Default) {
            val existingRecipeIdInDish = dish?.recipe?.recipeId
            val editableRecipe = _recipe.value
            val recipe = editableRecipe.toRecipe(existingRecipeIdInDish)
            runCatching {
                withContext(Dispatchers.IO) {
                    val newRecipeId = recipeRepository.upsertRecipe(recipe)
                    Timber.i("Recipe saved with id: $newRecipeId \n $recipe")
                    deleteRemovedSteps(editableRecipe, dish)
                    updateSteps(editableRecipe, existingRecipeIdInDish, newRecipeId)
                    updateDishWithRecipe(existingRecipeIdInDish, newRecipeId, dish)
                    refreshRecipeFromDatabase(newRecipeId, dish)
                }
            }.onSuccess {
                Timber.i("saveRecipe() Success")
                updateScreenState(ScreenState.Success())
            }.onFailure {
                Timber.e("saveRecipe() failure: $it")
            }
        }
    }


    /**
     * Identifies which steps user has removed and deletes them from DB.
     * */
    private suspend fun deleteRemovedSteps(editableRecipe: EditableRecipe, dish: DishDomain?) {
        Timber.i("deleteRemovedSteps()")
        val existingSteps = dish?.recipe?.steps?.map { it.id }
        val emptySteps =
            editableRecipe.steps.filter { it.stepDescription.isBlank() }.mapNotNull { it.id }
        Timber.i("emptyStepsIds : $emptySteps")
        val removedSteps =
            existingSteps?.filter { emptySteps.contains(it) }?.filterNotNull()
        Timber.i("removedSteps: $removedSteps")
        removedSteps?.let {
            recipeRepository.deleteRecipeStepsByIds(it)
        }
    }

    private suspend fun updateSteps(
        editableRecipe: EditableRecipe, existingRecipeIdInDish: Long?, newRecipeId: Long
    ) {
        Timber.i("updateSteps()")
        val steps = editableRecipe.steps.filter { it.stepDescription.isNotBlank() }
            .mapIndexed { index, step ->
                step.toRecipeStep(
                    recipeId = existingRecipeIdInDish ?: newRecipeId, newOrder = index
                )
            }
        recipeRepository.upsertRecipeSteps(steps)
        Timber.i("Steps saved: \n $steps")
    }

    private suspend fun updateDishWithRecipe(existingRecipeIdInDish: Long?, newRecipeId: Long, dish: DishDomain?) {
        if (existingRecipeIdInDish == null) {
            Timber.i("Recipe created, updating dish with recipe: $newRecipeId")
            dish?.id?.let { dishRepository.updateDishRecipe(newRecipeId, it) }
                ?: throw DishNotFound()
        } else {
            Timber.i("Recipe updated, skipping dish update")
        }
    }

    /**
     * Fetches recipe and updates [dish] recipe. Necessary after [saveRecipe].
     * If recipe was updated, newRecipeId is -1, then takes recipeId from existing recipe.
     * Otherwise uses newly created recipe.
     * */
    private suspend fun refreshRecipeFromDatabase(newRecipeId: Long, dish: DishDomain?) {
        dish ?: return
        val id: Long? = if (newRecipeId == -1L) dish.recipe?.recipeId else newRecipeId
        Timber.i("refreshDishRecipe($newRecipeId)")
        id?.let {
            val updatedRecipe = recipeRepository.getRecipeWithSteps(it)
            updateDish(dish.copy(recipe = updatedRecipe.toRecipeDomain()))
            _recipe.update { updatedRecipe.toEditableRecipe() }
            Timber.i("Dish's recipe and recipe refreshed with ${updatedRecipe.toRecipeDomain()} \n${updatedRecipe.toRecipeDomain().steps}")
        }
    }
}