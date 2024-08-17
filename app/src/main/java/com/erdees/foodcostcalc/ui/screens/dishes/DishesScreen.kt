package com.erdees.foodcostcalc.ui.screens.dishes

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.navigation.NavController
import com.erdees.foodcostcalc.databinding.CompDishesBinding
import com.erdees.foodcostcalc.ui.navigation.FCCScreen
import com.erdees.foodcostcalc.ui.screens.products.ProductsFragment

@Composable
fun DishesScreen(navController: NavController, modifier: Modifier = Modifier) {
  Scaffold(
    modifier = modifier,
    floatingActionButton = {
      FloatingActionButton(
        onClick = {
          navController.navigate(FCCScreen.CreateDish)
        },
        shape = CircleShape,
      ) {
        Icon(Icons.Filled.Add, "Large floating action button")
      }
    }
  ) { paddingValues ->
    AndroidViewBinding(
      CompDishesBinding::inflate,
      modifier = Modifier.padding(paddingValues)
    ) {
      this.dishesScreenFragmentContainerView.getFragment<DishesFragment?>()?.navigateToAddItemToDishScreen =
        { dishId, dishName ->
          Log.i("DishesScreen", "Navigate to AddItemToDishScreen")
          navController.navigate(FCCScreen.AddItemsToDish(dishId, dishName))
        }
    }
  }
}

