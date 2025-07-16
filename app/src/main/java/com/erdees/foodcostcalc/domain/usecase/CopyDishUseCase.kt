package com.erdees.foodcostcalc.domain.usecase

import android.os.Bundle
import com.erdees.foodcostcalc.data.repository.AnalyticsRepository
import com.erdees.foodcostcalc.data.repository.DishRepository
import com.erdees.foodcostcalc.data.repository.HalfProductRepository
import com.erdees.foodcostcalc.data.repository.ProductRepository
import com.erdees.foodcostcalc.data.repository.RecipeRepository
import com.erdees.foodcostcalc.domain.mapper.Mapper.toDishBase
import com.erdees.foodcostcalc.domain.mapper.Mapper.toHalfProductDish
import com.erdees.foodcostcalc.domain.mapper.Mapper.toProductDish
import com.erdees.foodcostcalc.domain.mapper.Mapper.toRecipe
import com.erdees.foodcostcalc.domain.mapper.Mapper.toSteps
import com.erdees.foodcostcalc.domain.model.dish.DishActionResult
import com.erdees.foodcostcalc.domain.model.dish.DishDetailsActionResultType
import com.erdees.foodcostcalc.domain.model.dish.DishDomain
import com.erdees.foodcostcalc.utils.Constants
import com.erdees.foodcostcalc.utils.MyDispatchers
import kotlinx.coroutines.withContext

class CopyDishUseCase(
    private val dishRepository: DishRepository,
    private val productRepository: ProductRepository,
    private val halfProductRepository: HalfProductRepository,
    private val recipeRepository: RecipeRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val myDispatchers: MyDispatchers
) {
    suspend operator fun invoke(dish: DishDomain, newName: String): Result<DishActionResult> =
        withContext(myDispatchers.ioDispatcher) {
            try {
                val copiedRecipeId = copyRecipeWithSteps(dish)
                val newDishId = createDishCopy(dish, newName, copiedRecipeId)
                copyDishProducts(dish, newDishId)
                copyDishHalfProducts(dish, newDishId)
                logDishCopyEvent(newName)

                Result.success(
                    DishActionResult(
                        type = DishDetailsActionResultType.COPIED,
                        dishId = newDishId
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    private suspend fun copyRecipeWithSteps(dish: DishDomain): Long? {
        val recipe = dish.recipe?.toRecipe() ?: return null
        val recipeId = recipeRepository.upsertRecipe(recipe)

        dish.recipe.toSteps().let { steps ->
            recipeRepository.upsertRecipeSteps(steps.map { it.copy(recipeId = recipeId) })
        }

        return recipeId
    }

    private suspend fun createDishCopy(dish: DishDomain, newName: String, recipeId: Long?): Long {
        val dishCopy = dish.copy(
            id = 0L,
            name = newName,
        ).toDishBase().copy(
            recipeId = recipeId
        )

        return dishRepository.addDish(dishCopy)
    }

    private suspend fun copyDishProducts(dish: DishDomain, newDishId: Long) {
        dish.products.forEach { product ->
            val productCopy = product.copy(
                id = 0L,
                ownerId = newDishId
            )
            productRepository.addProductDish(productCopy.toProductDish())
        }
    }

    private suspend fun copyDishHalfProducts(dish: DishDomain, newDishId: Long) {
        dish.halfProducts.forEach { halfProduct ->
            val halfProductCopy = halfProduct.copy(
                id = 0L,
                ownerId = newDishId
            )
            halfProductRepository.addHalfProductDish(halfProductCopy.toHalfProductDish())
        }
    }

    private fun logDishCopyEvent(dishName: String) {
        analyticsRepository.logEvent(Constants.Analytics.DishV2.COPY, Bundle().apply {
            putString(Constants.Analytics.DISH_NAME, dishName)
        })
    }
}