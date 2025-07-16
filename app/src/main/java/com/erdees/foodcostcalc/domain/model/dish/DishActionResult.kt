package com.erdees.foodcostcalc.domain.model.dish

data class DishActionResult(
    val type: DishDetailsActionResultType,
    val dishId: Long
)