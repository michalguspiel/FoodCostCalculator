package com.erdees.foodcostcalc.domain.model.recipe

import kotlinx.serialization.Serializable

@Serializable
data class RecipeDomain(
    val recipeId: Long,
    val prepTimeMinutes: Int,
    val cookTimeMinutes: Int,
    val description: String,
    val steps: List<String>,
    val tips: String
)