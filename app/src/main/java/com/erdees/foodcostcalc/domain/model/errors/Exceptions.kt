package com.erdees.foodcostcalc.domain.model.errors

data class DishNotFound(override val message: String = "Dish not found to bind with recipe") :
    Exception(message)
