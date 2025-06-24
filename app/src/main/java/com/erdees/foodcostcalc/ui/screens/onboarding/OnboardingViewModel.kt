package com.erdees.foodcostcalc.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.Preferences
import com.erdees.foodcostcalc.data.model.local.DishBase
import com.erdees.foodcostcalc.data.model.local.ProductBase
import com.erdees.foodcostcalc.data.model.local.Recipe
import com.erdees.foodcostcalc.data.model.local.RecipeStep
import com.erdees.foodcostcalc.data.model.local.associations.ProductDish
import com.erdees.foodcostcalc.data.repository.DishRepository
import com.erdees.foodcostcalc.data.repository.ProductRepository
import com.erdees.foodcostcalc.data.repository.RecipeRepository
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
    data class Error(val message: String) : OnboardingUiState()
}

class OnboardingViewModel : ViewModel(), KoinComponent {
    private val productRepository: ProductRepository by inject()
    private val dishRepository: DishRepository by inject()
    private val recipeRepository: RecipeRepository by inject()
    private val preferences: Preferences by inject()

    private val _uiState = MutableStateFlow<OnboardingUiState>(OnboardingUiState.Idle)
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun createSampleDishAndNavigate() {
        viewModelScope.launch {
            _uiState.value = OnboardingUiState.Loading
            Timber.i("Creating sample dish...")
            try {
                val addedProducts = sampleIngredients().map { ingredient ->
                    val id = productRepository.addProduct(ingredient)
                    ingredient.copy(productId = id)
                }

                // Add a sample recipe using RecipeRepository
                val recipe = Recipe(
                    0,
                    prepTimeMinutes = 10,
                    cookTimeMinutes = 8,
                    description = "A classic cheeseburger recipe.",
                    tips = "Use fresh beef for best results."
                )
                val recipeId = recipeRepository.upsertRecipe(recipe)

                // Add sample steps
                val steps = listOf(
                    RecipeStep(0, recipeId, "Shape the beef into a patty.", 1),
                    RecipeStep(0, recipeId, "Grill the patty.", 2),
                    RecipeStep(0, recipeId, "Toast the bun.", 3),
                    RecipeStep(0, recipeId, "Assemble with cheese, lettuce, and bun.", 4)
                )
                recipeRepository.upsertRecipeSteps(steps)

                val dish = sampleDish(recipeId)
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
                // Mark onboarding as seen
                preferences.setHasSeenExampleDishOnboarding(true)
                _uiState.value = OnboardingUiState.Success(dishId)
                Timber.i("Sample dish created successfully with id $dishId. Navigating...")
            } catch (e: Exception) {
                _uiState.value = OnboardingUiState.Error(e.message ?: "Unknown error")
                Timber.e(e, "Error creating sample dish.")
            }
        }
    }

    private fun sampleIngredients() = listOf(
        ProductBase(0, "Minced Beef", 19.20, 0.0, 0.0, "per kilogram"),
        ProductBase(0, "Burger Bun", 0.7, 0.0, 0.0, "per piece"),
        ProductBase(0, "Cheese Slice", 0.5, 0.0, 0.0, "per piece"),
        ProductBase(0, "Lettuce", 3.99, 0.0, 15.0, "per kilogram")
    )

    private fun sampleDish(recipeId: Long) = DishBase(
        0, "Classic Cheeseburger", marginPercent = 360.0, dishTax = 12.0, recipeId = recipeId
    )

    fun resetUiState() {
        _uiState.value = OnboardingUiState.Idle
    }
}
