package com.erdees.foodcostcalc.ui.screens.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.navigation.NavController
import com.erdees.foodcostcalc.databinding.CompSettingsBinding

@Composable
fun SettingsScreen(navController: NavController) {

  AndroidViewBinding(CompSettingsBinding::inflate)
}
