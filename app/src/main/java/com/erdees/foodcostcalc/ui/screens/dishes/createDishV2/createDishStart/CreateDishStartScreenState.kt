package com.erdees.foodcostcalc.ui.screens.dishes.createDishV2.createDishStart

import android.icu.util.Currency
import androidx.annotation.StringRes
import com.erdees.foodcostcalc.domain.model.product.ProductAddedToDish
import com.erdees.foodcostcalc.domain.model.product.ProductDomain

data class CreateDishStartScreenState(
    val dishName: String,
    val addedProducts: List<ProductAddedToDish> = listOf(),
    val currency: Currency?,
    val isFirstDish: Boolean,
    @StringRes val errorRes: Int?,
)