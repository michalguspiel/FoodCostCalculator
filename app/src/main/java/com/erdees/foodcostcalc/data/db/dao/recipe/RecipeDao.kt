package com.erdees.foodcostcalc.data.db.dao.recipe

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.erdees.foodcostcalc.data.model.Recipe
import com.erdees.foodcostcalc.data.model.RecipeStep
import com.erdees.foodcostcalc.data.model.joined.RecipeWithSteps

@Dao
interface RecipeDao {
    @Upsert
    suspend fun upsertRecipe(recipe: Recipe): Long

    @Upsert
    suspend fun upsert(recipeStep: List<RecipeStep>)

    @Transaction
    @Query("SELECT * FROM Recipe WHERE recipeId =:id")
    suspend fun getRecipeWithSteps(id: Long): RecipeWithSteps

    @Query("DELETE FROM Recipe_Step WHERE id IN (:ids)")
    suspend fun deleteRecipeStepsByIds(ids: List<Long>)
}