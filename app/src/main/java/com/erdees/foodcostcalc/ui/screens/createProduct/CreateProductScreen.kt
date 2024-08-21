package com.erdees.foodcostcalc.ui.screens.createProduct

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.erdees.foodcostcalc.databinding.CompCreateProductBinding

@Composable
fun CreateProductScreen(modifier: Modifier = Modifier) {
  AndroidViewBinding(CompCreateProductBinding::inflate)
}
