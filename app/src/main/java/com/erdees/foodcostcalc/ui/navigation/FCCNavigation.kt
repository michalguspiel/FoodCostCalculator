package com.erdees.foodcostcalc.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.erdees.foodcostcalc.domain.model.dish.DishDomain
import com.erdees.foodcostcalc.domain.model.dish.DishDomainNavType
import com.erdees.foodcostcalc.ui.screens.createProduct.CreateProductScreen
import com.erdees.foodcostcalc.ui.screens.dishes.DishesScreen
import com.erdees.foodcostcalc.ui.screens.dishes.addItemToDish.AddItemToDishScreen
import com.erdees.foodcostcalc.ui.screens.dishes.createDish.CreateDishScreen
import com.erdees.foodcostcalc.ui.screens.dishes.editDish.EditDishScreen
import com.erdees.foodcostcalc.ui.screens.products.ProductsScreen
import com.erdees.foodcostcalc.ui.screens.settings.SettingsScreen
import kotlin.reflect.typeOf

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
    composable<FCCScreen.AddItemsToDish> { backStackEntry ->
      val addItemsToDish: FCCScreen.AddItemsToDish = backStackEntry.toRoute()
      AddItemToDishScreen(dishId = addItemsToDish.dishId, dishName = addItemsToDish.dishName)
    }

    composable<FCCScreen.EditDish>(
      typeMap = mapOf(typeOf<DishDomain>() to DishDomainNavType)
    ) { backStackEntry ->
      val editDish: FCCScreen.EditDish = backStackEntry.toRoute()
      EditDishScreen(dishDomain = editDish.dishDomain, navController = navController)
    }

    composable<FCCScreen.CreateProduct> {
      CreateProductScreen()
    }
    composable<FCCScreen.CreateDish> {
      CreateDishScreen()
    }
  }
}
