package com.erdees.foodcostcalc.data.repository

import com.erdees.foodcostcalc.data.db.dao.recipe.RecipeDao
import com.erdees.foodcostcalc.data.model.Recipe
import com.erdees.foodcostcalc.data.model.RecipeStep
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface RecipeRepository {
    suspend fun addRecipe(recipe: Recipe): Long
    suspend fun addRecipeSteps(recipeStep: List<RecipeStep>)
}

class RecipeRepositoryImpl : RecipeRepository, KoinComponent {

    private val recipeDao: RecipeDao by inject()

    override suspend fun addRecipe(recipe: Recipe): Long {
        return recipeDao.addRecipe(recipe)
    }

    override suspend fun addRecipeSteps(recipeStep: List<RecipeStep>) {
        recipeDao.addRecipeSteps(recipeStep)
    }
}