package com.erdees.foodcostcalc.ui.screens.dishes.createDishV2.createDishSummary

import android.icu.util.Currency
import androidx.annotation.StringRes
import com.erdees.foodcostcalc.domain.model.ItemUsageEntry

data class CreateDishSummaryScreenState(
    val dishName: String = "",
    val addedComponents: List<ItemUsageEntry> = emptyList(),
    val foodCost: Double = 0.0,
    val marginPercent: String,
    val taxPercent: String,
    val finalSellingPrice: Double = 0.0,
    val currency: Currency?,
    val isLoading : Boolean,
    @StringRes val errorRes: Int?,
    val successfullySavedDishId: Long?
)