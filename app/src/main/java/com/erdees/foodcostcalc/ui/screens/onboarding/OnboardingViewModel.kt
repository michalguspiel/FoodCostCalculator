package com.erdees.foodcostcalc.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.Preferences
import com.erdees.foodcostcalc.data.model.local.DishBase
import com.erdees.foodcostcalc.data.model.local.ProductBase
import com.erdees.foodcostcalc.data.model.local.associations.ProductDish
import com.erdees.foodcostcalc.data.repository.DishRepository
import com.erdees.foodcostcalc.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

sealed class OnboardingUiState {
    data object Idle : OnboardingUiState()
    data object Loading : OnboardingUiState()
    data class Success(val dishId: Long) : OnboardingUiState()
    data class Error(val message: String) : OnboardingUiState()
}

class OnboardingViewModel : ViewModel(), KoinComponent {
    private val productRepository: ProductRepository by inject()
    private val dishRepository: DishRepository by inject()
    private val preferences: Preferences by inject()

    private val _uiState = MutableStateFlow<OnboardingUiState>(OnboardingUiState.Idle)
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun createSampleDishAndNavigate() {
        viewModelScope.launch {
            _uiState.value = OnboardingUiState.Loading
            try {
                val addedProducts = sampleIngredients().map { ingredient ->
                    val id = productRepository.addProduct(ingredient)
                    ingredient.copy(productId = id)
                }

                val dish = sampleDish()
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
            } catch (e: Exception) {
                _uiState.value = OnboardingUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun sampleIngredients() = listOf(
        ProductBase(0, "Minced Beef", 19.20, 0.0, 0.0, "per kilogram"),
        ProductBase(0, "Burger Bun", 0.7, 0.0, 0.0, "per piece"),
        ProductBase(0, "Cheese Slice", 0.5, 0.0, 0.0, "per piece"),
        ProductBase(0, "Lettuce", 3.99, 0.0, 15.0, "per kilogram")
    )

    private fun sampleDish() = DishBase(
        0, "Classic Cheeseburger", marginPercent = 360.0, dishTax = 12.0, recipeId = null
    )

    fun resetUiState() {
        _uiState.value = OnboardingUiState.Idle
    }
}
