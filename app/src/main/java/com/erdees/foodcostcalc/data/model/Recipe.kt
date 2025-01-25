package com.erdees.foodcostcalc.data.model

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

@Keep
@Entity(tableName = "Recipe")
data class Recipe(
    @PrimaryKey(autoGenerate = true) val recipeId: Long,
    val prepTimeMinutes: Int,
    val cookTimeMinutes: Int,
    val description: String,
    val tips: String
)