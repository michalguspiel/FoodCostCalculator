package com.erdees.foodcostcalc.data.model.joined

import androidx.room.Embedded
import androidx.room.Relation
import com.erdees.foodcostcalc.data.model.Recipe
import com.erdees.foodcostcalc.data.model.RecipeStep

data class RecipeWithSteps(
    @Embedded val recipe: Recipe,
    @Relation(
        parentColumn = "recipeId",
        entityColumn = "recipeId"
    )
    val steps: List<RecipeStep>
)