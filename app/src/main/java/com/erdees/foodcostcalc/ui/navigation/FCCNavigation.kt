package com.erdees.foodcostcalc.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.erdees.foodcostcalc.ui.screens.dishes.DishesScreen
import com.erdees.foodcostcalc.ui.screens.dishes.addItemToDish.AddItemToDishScreen
import com.erdees.foodcostcalc.ui.screens.dishes.createDish.CreateDishScreen
import com.erdees.foodcostcalc.ui.screens.dishes.editDish.EditDishScreen
import com.erdees.foodcostcalc.ui.screens.dishes.editDish.DishDetailsViewModel
import com.erdees.foodcostcalc.ui.screens.halfProducts.HalfProductsScreen
import com.erdees.foodcostcalc.ui.screens.halfProducts.addItemToHalfProduct.AddItemToHalfProductScreen
import com.erdees.foodcostcalc.ui.screens.halfProducts.editHalfProduct.EditHalfProductScreen
import com.erdees.foodcostcalc.ui.screens.onlineBackup.DataBackupScreen
import com.erdees.foodcostcalc.ui.screens.products.ProductsScreen
import com.erdees.foodcostcalc.ui.screens.products.createProduct.CreateProductScreen
import com.erdees.foodcostcalc.ui.screens.products.editProduct.EditProductScreen
import com.erdees.foodcostcalc.ui.screens.recipe.RecipeScreen
import com.erdees.foodcostcalc.ui.screens.settings.SettingsScreen
import com.erdees.foodcostcalc.ui.screens.subscriptionScreen.SubscriptionScreen

@Composable
fun FCCNavigation(
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = FCCScreen.Products,
        modifier = modifier
            .padding(paddingValues)
            .consumeWindowInsets(paddingValues)
    ) {
        composable<FCCScreen.Products> {
            ProductsScreen(navController = navController)
        }
        composable<FCCScreen.HalfProducts> {
            HalfProductsScreen(navController = navController)
        }
        composable<FCCScreen.Dishes> {
            DishesScreen(navController = navController)
        }
        composable<FCCScreen.Settings> {
            SettingsScreen(navController = navController)
        }
        composable<FCCScreen.DataBackup> {
            DataBackupScreen(navController = navController)
        }

        composable<FCCScreen.AddItemToHalfProduct> { backStackEntry ->
            val route: FCCScreen.AddItemToHalfProduct = backStackEntry.toRoute()
            AddItemToHalfProductScreen(
                navController = navController,
                halfProductId = route.id,
                halfProductName = route.name,
                halfProductUnit = route.unit
            )
        }
        composable<FCCScreen.AddItemsToDish> { backStackEntry ->
            val addItemsToDish: FCCScreen.AddItemsToDish = backStackEntry.toRoute()
            AddItemToDishScreen(
                navController = navController,
                dishId = addItemsToDish.dishId,
                dishName = addItemsToDish.dishName
            )
        }

        composable<FCCScreen.EditDish> { backStackEntry ->
            val route: FCCScreen.EditDish = backStackEntry.toRoute()
            EditDishScreen(dishId = route.dishId, navController = navController)
        }

         composable<FCCScreen.Recipe> { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(navController.previousBackStackEntry?.destination?.route.toString())
            }

            val viewModel = viewModel<DishDetailsViewModel>(parentEntry)
            RecipeScreen(navController, viewModel)
        }

        composable<FCCScreen.EditHalfProduct> { backStackEntry ->
            val route: FCCScreen.EditHalfProduct = backStackEntry.toRoute()
            EditHalfProductScreen(
                navController = navController,
                halfProductId = route.halfProductId
            )
        }

        composable<FCCScreen.CreateProduct> {
            CreateProductScreen(navController = navController)
        }
        composable<FCCScreen.CreateDish> {
            CreateDishScreen(navController = navController)
        }

        composable<FCCScreen.EditProduct> { backStackEntry ->
            val route: FCCScreen.EditProduct = backStackEntry.toRoute()
            EditProductScreen(route.productId, navController = navController)
        }

        composable<FCCScreen.Subscription> {
            SubscriptionScreen(navController)
        }
    }
}
