package com.erdees.foodcostcalc.ui.screens.dishes.createDishV2.createDishStart

import android.icu.util.Currency
import androidx.annotation.StringRes
import com.erdees.foodcostcalc.domain.model.ItemUsageEntry

data class CreateDishStartScreenState(
    val dishName: String,
    val addedComponents: List<ItemUsageEntry> = listOf(),
    val currency: Currency?,
    val isFirstDish: Boolean,
    @StringRes val errorRes: Int?,
)