package com.erdees.foodcostcalc.ui.navigation

import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.erdees.foodcostcalc.ui.screens.createProduct.CreateProductScreen
import com.erdees.foodcostcalc.ui.screens.dishes.createDish.CreateDishScreen
import com.erdees.foodcostcalc.ui.screens.dishes.DishesScreen
import com.erdees.foodcostcalc.ui.screens.dishes.addItemToDish.AddItemToDishScreen
import com.erdees.foodcostcalc.ui.screens.products.ProductsScreen
import com.erdees.foodcostcalc.ui.screens.settings.SettingsScreen

@Composable
fun FCCNavigation(
  paddingValues: PaddingValues,
  modifier: Modifier = Modifier,
  navController: NavHostController = rememberNavController(),
) {

  NavHost(
    navController = navController,
    startDestination = FCCScreen.Products,
    modifier = modifier.padding(paddingValues)
  ) {
    composable<FCCScreen.Products> {
      ProductsScreen(navController = navController)
    }
    composable<FCCScreen.HalfProducts> {
    /* TODO: Implement HalfProducts screen UI */
    }
    composable<FCCScreen.Dishes> {
      DishesScreen(navController = navController)
    }
    composable<FCCScreen.Settings> {
      SettingsScreen(navController = navController)
    }
    composable<FCCScreen.OnlineData> {
    /* TODO: Implement OnlineData screen UI */
    }

    composable<FCCScreen.AddProductToHalfProduct> {
    /* TODO: Implement AddProductToHalfProduct screen UI */
    }
    composable<FCCScreen.AddItemsToDish> {backStackEntry ->
      Log.i("FCCNavigation", "AddItemsToDish")
      val addItemsToDish: FCCScreen.AddItemsToDish = backStackEntry.toRoute()
      AddItemToDishScreen(dishId = addItemsToDish.dishId, dishName = addItemsToDish.dishName)
    }

    composable<FCCScreen.CreateProduct> {
      CreateProductScreen()
    }
    composable<FCCScreen.CreateDish> {
      CreateDishScreen()
    }
  }
}
