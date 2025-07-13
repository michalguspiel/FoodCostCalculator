package com.erdees.foodcostcalc.domain.model.dish

data class DishActionResult(
    val type: DishActionResultType,
    val dishId: Long
)