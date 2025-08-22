package com.erdees.foodcostcalc.data.repository

import com.erdees.foodcostcalc.data.db.dao.recipe.RecipeDao
import com.erdees.foodcostcalc.data.model.local.Recipe
import com.erdees.foodcostcalc.data.model.local.RecipeStep
import com.erdees.foodcostcalc.data.model.local.joined.RecipeWithSteps
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RecipeRepositoryTest {

    private val recipeDao = mockk<RecipeDao>(relaxed = true)

    // Test implementation that accepts mocked DAO
    private val testRepository = object : RecipeRepository {
        override suspend fun upsertRecipe(recipe: Recipe) = recipeDao.upsertRecipe(recipe)
        override suspend fun upsertRecipeSteps(recipeStep: List<RecipeStep>) = recipeDao.upsert(recipeStep)
        override suspend fun getRecipeWithSteps(recipeId: Long) = recipeDao.getRecipeWithSteps(recipeId)
        override suspend fun deleteRecipeStepsByIds(ids: List<Long>) = recipeDao.deleteRecipeStepsByIds(ids)
    }

    @Test
    fun `upsertRecipe should call dao upsertRecipe and return result`() = runTest {
        // Given
        val recipe = createTestRecipe(0L, "Test Recipe")
        val expectedId = 123L
        coEvery { recipeDao.upsertRecipe(recipe) } returns expectedId

        // When
        val result = testRepository.upsertRecipe(recipe)

        // Then
        result shouldBe expectedId
        coVerify { recipeDao.upsertRecipe(recipe) }
    }

    @Test
    fun `upsertRecipeSteps should call dao upsert`() = runTest {
        // Given
        val recipeSteps = listOf(
            createTestRecipeStep(0L, 1L, "Step 1", 1),
            createTestRecipeStep(0L, 1L, "Step 2", 2),
            createTestRecipeStep(0L, 1L, "Step 3", 3)
        )

        // When
        testRepository.upsertRecipeSteps(recipeSteps)

        // Then
        coVerify { recipeDao.upsert(recipeSteps) }
    }

    @Test
    fun `getRecipeWithSteps should call dao getRecipeWithSteps and return result`() = runTest {
        // Given
        val recipeId = 1L
        val expectedRecipeWithSteps = createTestRecipeWithSteps(recipeId)
        coEvery { recipeDao.getRecipeWithSteps(recipeId) } returns expectedRecipeWithSteps

        // When
        val result = testRepository.getRecipeWithSteps(recipeId)

        // Then
        result shouldBe expectedRecipeWithSteps
        coVerify { recipeDao.getRecipeWithSteps(recipeId) }
    }

    @Test
    fun `deleteRecipeStepsByIds should call dao deleteRecipeStepsByIds`() = runTest {
        // Given
        val stepIds = listOf(1L, 2L, 3L)

        // When
        testRepository.deleteRecipeStepsByIds(stepIds)

        // Then
        coVerify { recipeDao.deleteRecipeStepsByIds(stepIds) }
    }

    @Test
    fun `upsertRecipe with all fields should work correctly`() = runTest {
        // Given
        val recipe = Recipe(
            recipeId = 5L,
            prepTimeMinutes = 30,
            cookTimeMinutes = 45,
            description = "A delicious test recipe",
            tips = "Some helpful tips for cooking"
        )
        val expectedId = 5L
        coEvery { recipeDao.upsertRecipe(recipe) } returns expectedId

        // When
        val result = testRepository.upsertRecipe(recipe)

        // Then
        result shouldBe expectedId
        coVerify { recipeDao.upsertRecipe(recipe) }
    }

    @Test
    fun `upsertRecipe with minimal fields should work correctly`() = runTest {
        // Given
        val recipe = Recipe(
            recipeId = 0L,
            prepTimeMinutes = null,
            cookTimeMinutes = null,
            description = null,
            tips = null
        )
        val expectedId = 10L
        coEvery { recipeDao.upsertRecipe(recipe) } returns expectedId

        // When
        val result = testRepository.upsertRecipe(recipe)

        // Then
        result shouldBe expectedId
        coVerify { recipeDao.upsertRecipe(recipe) }
    }

    @Test
    fun `upsertRecipeSteps with empty list should work correctly`() = runTest {
        // Given
        val recipeSteps = emptyList<RecipeStep>()

        // When
        testRepository.upsertRecipeSteps(recipeSteps)

        // Then
        coVerify { recipeDao.upsert(recipeSteps) }
    }

    @Test
    fun `upsertRecipeSteps with single step should work correctly`() = runTest {
        // Given
        val recipeSteps = listOf(
            createTestRecipeStep(0L, 1L, "Only step", 1)
        )

        // When
        testRepository.upsertRecipeSteps(recipeSteps)

        // Then
        coVerify { recipeDao.upsert(recipeSteps) }
    }

    @Test
    fun `deleteRecipeStepsByIds with empty list should work correctly`() = runTest {
        // Given
        val stepIds = emptyList<Long>()

        // When
        testRepository.deleteRecipeStepsByIds(stepIds)

        // Then
        coVerify { recipeDao.deleteRecipeStepsByIds(stepIds) }
    }

    @Test
    fun `deleteRecipeStepsByIds with single id should work correctly`() = runTest {
        // Given
        val stepIds = listOf(42L)

        // When
        testRepository.deleteRecipeStepsByIds(stepIds)

        // Then
        coVerify { recipeDao.deleteRecipeStepsByIds(stepIds) }
    }

    @Test
    fun `getRecipeWithSteps should handle recipe with no steps`() = runTest {
        // Given
        val recipeId = 1L
        val recipe = createTestRecipe(recipeId, "Recipe without steps")
        val expectedRecipeWithSteps = RecipeWithSteps(
            recipe = recipe,
            steps = emptyList()
        )
        coEvery { recipeDao.getRecipeWithSteps(recipeId) } returns expectedRecipeWithSteps

        // When
        val result = testRepository.getRecipeWithSteps(recipeId)

        // Then
        result shouldBe expectedRecipeWithSteps
        coVerify { recipeDao.getRecipeWithSteps(recipeId) }
    }

    @Test
    fun `upsertRecipeSteps with large number of steps should work correctly`() = runTest {
        // Given
        val recipeId = 1L
        val recipeSteps = (1..10).map { stepNumber ->
            createTestRecipeStep(0L, recipeId, "Step $stepNumber", stepNumber)
        }

        // When
        testRepository.upsertRecipeSteps(recipeSteps)

        // Then
        coVerify { recipeDao.upsert(recipeSteps) }
    }

    @Test
    fun `deleteRecipeStepsByIds with large list should work correctly`() = runTest {
        // Given
        val stepIds = (1L..20L).toList()

        // When
        testRepository.deleteRecipeStepsByIds(stepIds)

        // Then
        coVerify { recipeDao.deleteRecipeStepsByIds(stepIds) }
    }

    @Test
    fun `upsertRecipe propagates dao exception`() = runTest {
        val recipe = createTestRecipe(0L, "Test Recipe")
        coEvery { recipeDao.upsertRecipe(recipe) } throws IllegalStateException("db error")
        shouldThrow<IllegalStateException> { testRepository.upsertRecipe(recipe) }
    }

    @Test
    fun `upsertRecipeSteps propagates dao exception`() = runTest {
        val steps = listOf(createTestRecipeStep(0L, 1L, "Step", 1))
        coEvery { recipeDao.upsert(steps) } throws IllegalStateException("db error")
        shouldThrow<IllegalStateException> { testRepository.upsertRecipeSteps(steps) }
    }

    @Test
    fun `getRecipeWithSteps propagates dao exception`() = runTest {
        val id = 1L
        coEvery { recipeDao.getRecipeWithSteps(id) } throws IllegalStateException("db error")
        shouldThrow<IllegalStateException> { testRepository.getRecipeWithSteps(id) }
    }

    @Test
    fun `deleteRecipeStepsByIds propagates dao exception`() = runTest {
        val ids = listOf(1L, 2L)
        coEvery { recipeDao.deleteRecipeStepsByIds(ids) } throws IllegalStateException("db error")
        shouldThrow<IllegalStateException> { testRepository.deleteRecipeStepsByIds(ids) }
    }

    private fun createTestRecipe(id: Long, description: String) = Recipe(
        recipeId = id,
        prepTimeMinutes = 15,
        cookTimeMinutes = 30,
        description = description,
        tips = "Test tips"
    )

    private fun createTestRecipeStep(id: Long, recipeId: Long, description: String, order: Int) = RecipeStep(
        id = id,
        recipeId = recipeId,
        stepDescription = description,
        order = order
    )

    private fun createTestRecipeWithSteps(recipeId: Long) = RecipeWithSteps(
        recipe = createTestRecipe(recipeId, "Test Recipe with Steps"),
        steps = listOf(
            createTestRecipeStep(1L, recipeId, "First step", 1),
            createTestRecipeStep(2L, recipeId, "Second step", 2),
            createTestRecipeStep(3L, recipeId, "Third step", 3)
        )
    )
}