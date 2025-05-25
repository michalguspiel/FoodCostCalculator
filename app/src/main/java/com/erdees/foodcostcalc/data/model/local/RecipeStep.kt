package com.erdees.foodcostcalc.data.model.local

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Keep
@Entity(tableName = "Recipe_Step",
    indices = [Index("recipeId")],
    foreignKeys = [
    ForeignKey(
        entity = Recipe::class,
        parentColumns = ["recipeId"],
        childColumns = ["recipeId"],
        onDelete = ForeignKey.CASCADE
    ),
])
data class RecipeStep(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val recipeId: Long,
    val stepDescription: String,
    val order: Int
)