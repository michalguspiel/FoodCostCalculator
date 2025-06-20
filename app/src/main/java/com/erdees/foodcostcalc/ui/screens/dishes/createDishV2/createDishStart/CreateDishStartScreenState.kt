package com.erdees.foodcostcalc.ui.screens.dishes.createDishV2.createDishStart

import android.icu.util.Currency
import com.erdees.foodcostcalc.domain.model.product.ProductAddedToDish
import com.erdees.foodcostcalc.domain.model.product.ProductDomain

data class CreateDishStartScreenState(
    val dishName: String,
    val newProductName: String,
    val shouldShowSuggestedProducts: Boolean,
    val addedProducts: List<ProductAddedToDish> = listOf(),
    val suggestedProducts: List<ProductDomain>? = listOf(),
    val selectedSuggestedProduct: ProductDomain? = null,
    val currency: Currency?,
    val isFirstDish: Boolean
)