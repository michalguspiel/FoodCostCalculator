package com.erdees.foodcostcalc.domain.model.recipe

import kotlinx.serialization.Serializable

@Serializable
data class RecipeDomain(
    val recipeId: Long? = null,
    val prepTimeMinutes: Int? = null,
    val cookTimeMinutes: Int? = null,
    val description: String? = null,
    val steps: List<String>? = null,
    val tips: String? = null
)

@Serializable
data class EditableRecipe(
    val prepTimeMinutes: String = "",
    val cookTimeMinutes: String = "",
    val description: String = "",
    val steps: List<String> = listOf(),
    val tips: String = ""
)