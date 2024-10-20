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
import com.erdees.foodcostcalc.domain.model.halfProduct.HalfProductDomain
import com.erdees.foodcostcalc.domain.model.halfProduct.HalfProductDomainNavType
import com.erdees.foodcostcalc.domain.model.product.ProductDomain
import com.erdees.foodcostcalc.domain.model.product.ProductDomainNavType
import com.erdees.foodcostcalc.ui.screens.dishes.DishesScreen
import com.erdees.foodcostcalc.ui.screens.dishes.addItemToDish.AddItemToDishScreen
import com.erdees.foodcostcalc.ui.screens.dishes.createDish.CreateDishScreen
import com.erdees.foodcostcalc.ui.screens.dishes.editDish.EditDishScreen
import com.erdees.foodcostcalc.ui.screens.halfProducts.HalfProductsScreen
import com.erdees.foodcostcalc.ui.screens.halfProducts.addItemToHalfProduct.AddItemToHalfProductScreen
import com.erdees.foodcostcalc.ui.screens.halfProducts.editHalfProduct.EditHalfProductScreen
import com.erdees.foodcostcalc.ui.screens.onlineBackup.DataBackupScreen
import com.erdees.foodcostcalc.ui.screens.products.ProductsScreen
import com.erdees.foodcostcalc.ui.screens.products.createProduct.CreateProductScreen
import com.erdees.foodcostcalc.ui.screens.products.editProduct.EditProductScreen
import com.erdees.foodcostcalc.ui.screens.settings.SettingsScreen
import com.erdees.foodcostcalc.ui.screens.subscriptionScreen.SubscriptionScreen
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

        composable<FCCScreen.AddItemToHalfProduct>(
            typeMap = mapOf(typeOf<HalfProductDomain>() to HalfProductDomainNavType)
        ) { backStackEntry ->
            val addItemToHalfProduct: FCCScreen.AddItemToHalfProduct = backStackEntry.toRoute()
            AddItemToHalfProductScreen(
                navController = navController,
                halfProductDomain = addItemToHalfProduct.halfProductDomain
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

        composable<FCCScreen.EditDish>(
            typeMap = mapOf(typeOf<DishDomain>() to DishDomainNavType)
        ) { backStackEntry ->
            val editDish: FCCScreen.EditDish = backStackEntry.toRoute()
            EditDishScreen(providedDishDomain = editDish.dishDomain, navController = navController)
        }

        composable<FCCScreen.EditHalfProduct>(
            typeMap = mapOf(typeOf<HalfProductDomain>() to HalfProductDomainNavType)
        ) { backStackEntry ->
            val editHalfProduct: FCCScreen.EditHalfProduct = backStackEntry.toRoute()
            EditHalfProductScreen(
                navController = navController,
                providedHalfProduct = editHalfProduct.halfProductDomain
            )
        }

        composable<FCCScreen.CreateProduct> {
            CreateProductScreen(navController = navController)
        }
        composable<FCCScreen.CreateDish> {
            CreateDishScreen(navController = navController)
        }

        composable<FCCScreen.EditProduct>(
            typeMap = mapOf(typeOf<ProductDomain>() to ProductDomainNavType)
        ) { backStackEntry ->
            val editProduct: FCCScreen.EditProduct = backStackEntry.toRoute()
            EditProductScreen(
                providedProduct = editProduct.productDomain,
                navController = navController
            )
        }

        composable<FCCScreen.Subscription> {
            SubscriptionScreen(navController)
        }
    }
}
