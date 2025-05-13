package com.erdees.foodcostcalc.domain.model.settings

import android.icu.util.Currency

data class UserSettings(
    val defaultMargin: String,
    val defaultTax: String,
    val currency: Currency?,
    val metricUsed: Boolean,
    val imperialUsed: Boolean,
    val showHalfProducts: Boolean
)