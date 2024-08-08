package com.erdees.foodcostcalc.domain.model

import androidx.annotation.Keep

@Keep
data class ProductDomain(
    val productId: Long,
    val name: String,
    val pricePerUnit: Double,
    val tax: Double,
    val waste: Double,
    val unit: String
)
