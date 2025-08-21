package com.erdees.foodcostcalc.ui.screens.limitreached

/**
 * Example usage of the LimitReachedScreen in a typical Android app scenario.
 * This demonstrates how to integrate the LimitReachedScreen into your navigation flow.
 */

// Example 1: Using in a NavHost with Compose Navigation
/*
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        // Other routes...
        
        composable("limit_reached/{subHeadline}") { backStackEntry ->
            val subHeadline = backStackEntry.arguments?.getString("subHeadline") ?: ""
            
            LimitReachedScreen(
                subHeadline = subHeadline,
                onDismiss = {
                    navController.popBackStack()
                },
                onSeePremium = {
                    navController.navigate("premium_features") {
                        popUpTo("limit_reached/{subHeadline}") { inclusive = true }
                    }
                }
            )
        }
    }
}
*/

// Example 2: Usage with ViewModel state management
/*
@Composable
fun DishCreationScreen(
    viewModel: DishCreationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    when (uiState) {
        is DishCreationUiState.LimitReached -> {
            LimitReachedScreen(
                subHeadline = uiState.message,
                onDismiss = { viewModel.dismissLimitReached() },
                onSeePremium = { viewModel.navigateToPremium() }
            )
        }
        // Other states...
    }
}
*/

// Example 3: Usage in a dialog or overlay context
/*
@Composable
fun MainScreen() {
    var showLimitReached by remember { mutableStateOf(false) }
    var limitMessage by remember { mutableStateOf("") }
    
    // Main content
    Box {
        // Your main screen content here
        
        // Limit reached overlay
        if (showLimitReached) {
            LimitReachedScreen(
                subHeadline = limitMessage,
                onDismiss = { showLimitReached = false },
                onSeePremium = { 
                    showLimitReached = false
                    // Navigate to premium features
                }
            )
        }
    }
}
*/

// Example messages for different limit types:
object LimitMessages {
    const val DISH_LIMIT = "You've successfully created 20 dishes—the maximum for the free plan."
    const val HALF_PRODUCT_LIMIT = "You've successfully created 2 half-products—the maximum for the free plan."
    const val INGREDIENT_LIMIT = "You've successfully created 50 ingredients—the maximum for the free plan."
    const val RECIPE_LIMIT = "You've successfully created 10 recipes—the maximum for the free plan."
}