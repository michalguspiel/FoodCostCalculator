package com.erdees.foodcostcalc.ui.screens.products.createProduct

data class CreateProductScreenActions(
    val addProduct: () -> Unit,
    val onCalculateWaste: () -> Unit,
    val onCalculatePiecePrice: () -> Unit,
    val resetScreenState: () -> Unit,
    val selectUnit: (String) -> Unit,
    val updateProductName: (String) -> Unit,
    val updateProductPrice: (String) -> Unit,
    val updateProductTax: (String) -> Unit,
    val updateProductWaste: (String) -> Unit,
    val calculateWaste: (Double?, Double?) -> Unit,
    val calculatePricePerPiece: (Double?, Int?) -> Unit,
)