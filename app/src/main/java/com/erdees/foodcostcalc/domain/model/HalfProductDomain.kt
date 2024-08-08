package com.erdees.foodcostcalc.domain.model

import androidx.annotation.Keep

@Keep
data class HalfProductDomain(
    val halfProductId: Long,
    val name: String,
    val halfProductUnit: String,
    val products: List<ProductDomain>
)
