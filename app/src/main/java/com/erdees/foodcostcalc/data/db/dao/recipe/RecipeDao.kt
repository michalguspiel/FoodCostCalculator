package com.erdees.foodcostcalc.data.db.dao.recipe

import androidx.room.Dao
import androidx.room.Upsert
import com.erdees.foodcostcalc.data.model.Recipe
import com.erdees.foodcostcalc.data.model.RecipeStep

@Dao
interface RecipeDao {
    @Upsert
    suspend fun upsertRecipe(recipe: Recipe): Long

    @Upsert
    suspend fun upsert(recipeStep: List<RecipeStep>)
}