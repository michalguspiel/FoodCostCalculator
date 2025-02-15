package com.erdees.foodcostcalc.utils

data class DishNotFound(override val message: String = "Dish not found to bind with recipe") :
    Exception(message)
