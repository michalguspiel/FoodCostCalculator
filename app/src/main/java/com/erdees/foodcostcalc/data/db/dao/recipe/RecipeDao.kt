package com.erdees.foodcostcalc.data.db.dao.recipe

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.erdees.foodcostcalc.data.model.Recipe
import com.erdees.foodcostcalc.data.model.RecipeStep

@Dao
interface RecipeDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun addRecipe(recipe: Recipe): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun addRecipeSteps(recipeStep: List<RecipeStep>)
}