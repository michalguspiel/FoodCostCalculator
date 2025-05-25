package com.erdees.foodcostcalc.data.model.local.joined

import androidx.room.Embedded
import androidx.room.Relation
import com.erdees.foodcostcalc.data.model.local.Recipe
import com.erdees.foodcostcalc.data.model.local.RecipeStep

data class RecipeWithSteps(
    @Embedded val recipe: Recipe,
    @Relation(
        parentColumn = "recipeId",
        entityColumn = "recipeId"
    )
    val steps: List<RecipeStep>
)