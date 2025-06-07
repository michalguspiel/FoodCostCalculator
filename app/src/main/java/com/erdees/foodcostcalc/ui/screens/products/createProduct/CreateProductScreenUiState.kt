package com.erdees.foodcostcalc.ui.screens.products.createProduct

import androidx.compose.material3.SnackbarHostState
import com.erdees.foodcostcalc.domain.model.ScreenState

data class CreateProductScreenUiState(
    val productName: String,
    val productPrice: String,
    val productTax: String,
    val productWaste: String,
    val units: Set<String>,
    val selectedUnit: String,
    val screenState: ScreenState,
    val isAddButtonEnabled: Boolean,
    val isCountPiecePriceEnabled: Boolean,
    val showTaxPercent: Boolean,
    val snackbarHostState: SnackbarHostState,
)