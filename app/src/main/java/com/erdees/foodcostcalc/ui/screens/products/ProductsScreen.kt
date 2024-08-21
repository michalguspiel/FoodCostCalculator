package com.erdees.foodcostcalc.ui.screens.products

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.navigation.NavController
import com.erdees.foodcostcalc.databinding.CompProductsBinding
import com.erdees.foodcostcalc.ui.navigation.FCCScreen
import com.erdees.foodcostcalc.utils.CallbackListener


@Composable
fun ProductsScreen(navController: NavController, modifier: Modifier = Modifier) {

  AndroidViewBinding(CompProductsBinding::inflate) {
    this.productsScreenFragmentContainerView.getFragment<ProductsFragment?>()?.callbackListener =
      object :
        CallbackListener {
        override fun callback() {
          navController.navigate(FCCScreen.CreateProduct)
        }
      }
  }
}
