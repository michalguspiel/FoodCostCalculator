package com.erdees.foodcostcalc.data.repository

import com.erdees.foodcostcalc.data.db.dao.recipe.RecipeDao
import com.erdees.foodcostcalc.data.model.Recipe
import com.erdees.foodcostcalc.data.model.RecipeStep
import com.erdees.foodcostcalc.data.model.joined.RecipeWithSteps
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface RecipeRepository {
    suspend fun upsertRecipe(recipe: Recipe): Long
    suspend fun upsertRecipeSteps(recipeStep: List<RecipeStep>)
    suspend fun getRecipeWithSteps(recipeId: Long): RecipeWithSteps
    suspend fun deleteRecipeStepsByIds(ids: List<Long>)
}

class RecipeRepositoryImpl : RecipeRepository, KoinComponent {

    private val recipeDao: RecipeDao by inject()

    override suspend fun upsertRecipe(recipe: Recipe): Long {
        return recipeDao.upsertRecipe(recipe)
    }

    override suspend fun upsertRecipeSteps(recipeStep: List<RecipeStep>) {
        recipeDao.upsert(recipeStep)
    }

    override suspend fun getRecipeWithSteps(recipeId: Long): RecipeWithSteps =
        recipeDao.getRecipeWithSteps(recipeId)

    override suspend fun deleteRecipeStepsByIds(ids: List<Long>) {
        recipeDao.deleteRecipeStepsByIds(ids)
    }
}