package com.erdees.foodcostcalc.domain.model.dish

import androidx.annotation.Keep

@Keep
data class DishPriceData(val totalPrice: Double, val margin: Double, val tax: Double)
