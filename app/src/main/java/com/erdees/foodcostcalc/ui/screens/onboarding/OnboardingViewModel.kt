package com.erdees.foodcostcalc.ui.screens.onboarding

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.data.Preferences
import com.erdees.foodcostcalc.data.model.local.DishBase
import com.erdees.foodcostcalc.data.model.local.ProductBase
import com.erdees.foodcostcalc.data.model.local.Recipe
import com.erdees.foodcostcalc.data.model.local.RecipeStep
import com.erdees.foodcostcalc.data.model.local.associations.ProductDish
import com.erdees.foodcostcalc.data.repository.DishRepository
import com.erdees.foodcostcalc.data.repository.ProductRepository
import com.erdees.foodcostcalc.data.repository.RecipeRepository
import com.erdees.foodcostcalc.domain.model.onboarding.OnboardingState
import com.erdees.foodcostcalc.ui.spotlight.Spotlight
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

sealed class OnboardingUiState {
    data object Idle : OnboardingUiState()
    data object Loading : OnboardingUiState()
    data class Success(val dishId: Long) : OnboardingUiState()
    data object Skipped : OnboardingUiState()
    data class Error(val message: String) : OnboardingUiState()
}

class OnboardingViewModel : ViewModel(), KoinComponent {
    private val productRepository: ProductRepository by inject()
    private val dishRepository: DishRepository by inject()
    private val recipeRepository: RecipeRepository by inject()
    private val preferences: Preferences by inject()
    val spotlight: Spotlight by inject()

    private val _uiState = MutableStateFlow<OnboardingUiState>(OnboardingUiState.Idle)
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun startOnboardingCreateSampleDishAndNavigate(context: Context) {
        viewModelScope.launch {
            _uiState.value = OnboardingUiState.Loading
            preferences.setOnboardingState(OnboardingState.STARTED)
            Timber.i("Creating sample dish...")
            try {
                val addedProducts = sampleIngredients(context).map { ingredient ->
                    val id = productRepository.addProduct(ingredient)
                    ingredient.copy(productId = id)
                }

                // Add a sample recipe using RecipeRepository
                val recipe = Recipe(
                    0,
                    prepTimeMinutes = 10,
                    cookTimeMinutes = 8,
                    description = context.getString(R.string.onboarding_recipe_description),
                    tips = context.getString(R.string.onboarding_recipe_tips)
                )
                val recipeId = recipeRepository.upsertRecipe(recipe)

                // Add sample steps
                val steps = listOf(
                    RecipeStep(0, recipeId, context.getString(R.string.onboarding_recipe_step1), 1),
                    RecipeStep(0, recipeId, context.getString(R.string.onboarding_recipe_step2), 2),
                    RecipeStep(0, recipeId, context.getString(R.string.onboarding_recipe_step3), 3),
                    RecipeStep(0, recipeId, context.getString(R.string.onboarding_recipe_step4), 4)
                )
                recipeRepository.upsertRecipeSteps(steps)

                val dish = sampleDish(recipeId, context)
                val dishId = dishRepository.addDish(dish)
                val productDishes = listOf(
                    ProductDish(0, addedProducts[0].productId, dishId, 150.0, "gram"),
                    ProductDish(0, addedProducts[1].productId, dishId, 1.0, "piece"),
                    ProductDish(0, addedProducts[2].productId, dishId, 1.0, "piece"),
                    ProductDish(0, addedProducts[3].productId, dishId, 20.0, "gram")
                )
                productDishes.forEach {
                    productRepository.addProductDish(it)
                }
                _uiState.value = OnboardingUiState.Success(dishId)
                Timber.i("Sample dish created successfully with id $dishId. Navigating...")
            } catch (e: Exception) {
                _uiState.value = OnboardingUiState.Error(e.message ?: context.getString(R.string.onboarding_unknown_error))
                Timber.e(e, "Error creating sample dish.")
            }
        }
    }

    fun onboardingSkipped() {
        viewModelScope.launch {
            _uiState.value = OnboardingUiState.Loading
            preferences.setOnboardingState(OnboardingState.SKIPPED)
            _uiState.value = OnboardingUiState.Skipped
        }
    }

    private fun sampleIngredients(context: Context) = listOf(
        ProductBase(0, context.getString(R.string.onboarding_ingredient_minced_beef), 19.20, 0.0, 0.0, "per kilogram"),
        ProductBase(0, context.getString(R.string.onboarding_ingredient_burger_bun), 0.7, 0.0, 0.0, "per piece"),
        ProductBase(0, context.getString(R.string.onboarding_ingredient_cheese_slice), 0.5, 0.0, 0.0, "per piece"),
        ProductBase(0, context.getString(R.string.onboarding_ingredient_lettuce), 3.99, 0.0, 15.0, "per kilogram")
    )

    private fun sampleDish(recipeId: Long, context: Context) = DishBase(
        0, context.getString(R.string.onboarding_dish_classic_cheeseburger), marginPercent = 360.0, dishTax = 12.0, recipeId = recipeId
    )

    fun resetUiState() {
        _uiState.value = OnboardingUiState.Idle
    }
}
