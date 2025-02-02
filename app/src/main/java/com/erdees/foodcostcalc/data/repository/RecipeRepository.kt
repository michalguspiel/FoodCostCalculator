package com.erdees.foodcostcalc.data.repository

import com.erdees.foodcostcalc.data.db.dao.recipe.RecipeDao
import com.erdees.foodcostcalc.data.model.Recipe
import com.erdees.foodcostcalc.data.model.RecipeStep
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface RecipeRepository {
    suspend fun upsertRecipe(recipe: Recipe): Long
    suspend fun upsertRecipeSteps(recipeStep: List<RecipeStep>)
}

class RecipeRepositoryImpl : RecipeRepository, KoinComponent {

    private val recipeDao: RecipeDao by inject()

    override suspend fun upsertRecipe(recipe: Recipe): Long {
        return recipeDao.upsertRecipe(recipe)
    }

    override suspend fun upsertRecipeSteps(recipeStep: List<RecipeStep>) {
        recipeDao.upsert(recipeStep)
    }
}