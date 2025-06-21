package com.erdees.foodcostcalc.ui.screens.dishes.createDishV2.createDishStart

import com.erdees.foodcostcalc.domain.model.product.ProductDomain

sealed class CreateDishIntent {
    data class AddNewProduct(val productName: String) : CreateDishIntent()
    data class AddProduct(val product: ProductDomain) : CreateDishIntent()
}