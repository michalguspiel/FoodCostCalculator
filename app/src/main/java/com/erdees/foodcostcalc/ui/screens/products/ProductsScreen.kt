package com.erdees.foodcostcalc.ui.screens.products

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.navigation.NavController
import com.erdees.foodcostcalc.databinding.CompProductsBinding
import com.erdees.foodcostcalc.ui.navigation.FCCScreen


@Composable
fun ProductsScreen(navController: NavController) {
    Scaffold(
        modifier = Modifier,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(FCCScreen.CreateProduct)
                },
                shape = CircleShape,
            ) {
                Icon(Icons.Filled.Add, "Large floating action button")
            }
        }
    ) { paddingValues ->
        AndroidViewBinding(
            CompProductsBinding::inflate,
            modifier = Modifier.padding(paddingValues)
        ){
            this.productsScreenFragmentContainerView.getFragment<ProductsFragment?>()?.onNavigateToEditProduct =
                { product ->
                    Log.i("DishesScreen", "Navigate to AddItemToDishScreen")
                    navController.navigate(FCCScreen.EditProduct(product))
                }
        }

    }
}