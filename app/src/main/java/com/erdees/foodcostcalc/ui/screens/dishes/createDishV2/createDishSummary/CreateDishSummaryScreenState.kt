package com.erdees.foodcostcalc.ui.screens.dishes.createDishV2.createDishSummary

import android.icu.util.Currency
import androidx.annotation.StringRes
import com.erdees.foodcostcalc.domain.model.product.ProductAddedToDish

data class CreateDishSummaryScreenState(
    val dishName: String = "",
    val addedProducts: List<ProductAddedToDish> = emptyList(),
    val foodCost: Double = 0.0,
    val marginPercent: String,
    val taxPercent: String,
    val finalSellingPrice: Double = 0.0,
    val currency: Currency?,
    val isLoading : Boolean,
    @StringRes val errorRes: Int?
)