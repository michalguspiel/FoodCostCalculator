package com.erdees.foodcostcalc.ui.screens.products.createProduct

import androidx.compose.material3.SnackbarHostState
import com.erdees.foodcostcalc.domain.model.ScreenState
import com.erdees.foodcostcalc.domain.model.units.MeasurementUnit

data class CreateProductScreenUiState(
    val productName: String,
    val productPrice: String,
    val productTax: String,
    val productWaste: String,
    val units: Set<MeasurementUnit>,
    val selectedUnit: MeasurementUnit?,
    val screenState: ScreenState,
    val isAddButtonEnabled: Boolean,
    val isCountPiecePriceEnabled: Boolean,
    val showTaxPercent: Boolean,
    val snackbarHostState: SnackbarHostState,
)