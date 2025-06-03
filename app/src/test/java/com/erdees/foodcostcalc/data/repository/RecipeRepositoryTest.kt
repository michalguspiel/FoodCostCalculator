package com.erdees.foodcostcalc.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.erdees.foodcostcalc.data.db.dao.recipe.RecipeDao
import com.erdees.foodcostcalc.data.model.local.Recipe
import com.erdees.foodcostcalc.data.model.local.RecipeStep
import com.erdees.foodcostcalc.data.model.local.joined.RecipeWithSteps
import com.erdees.foodcostcalc.utils.TestCoroutineRule
import io.kotest.matchers.shouldBe
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject

@ExperimentalCoroutinesApi
class RecipeRepositoryTest : KoinTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @RelaxedMockK
    lateinit var recipeDao: RecipeDao

    private val recipeRepository: RecipeRepository by inject()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        startKoin {
            modules(module {
                single { recipeDao }
                single<RecipeRepository> { RecipeRepositoryImpl() }
            })
        }
    }

    @After
    fun teardown() {
        stopKoin()
        unmockkAll()
    }

    @Test
    fun `upsertRecipe should call dao to upsert recipe and return id`() = runTest {
        // Given
        val recipe = Recipe(0L, 30, 60, "Test Recipe", "Some tips")
        val expectedId = 1L
        coEvery { recipeDao.upsertRecipe(recipe) } returns expectedId

        // When
        val resultId = recipeRepository.upsertRecipe(recipe)

        // Then
        resultId shouldBe expectedId
        coVerify { recipeDao.upsertRecipe(recipe) }
    }

    @Test
    fun `upsertRecipeSteps should call dao to upsert recipe steps`() = runTest {
        // Given
        val recipeSteps = listOf(
            RecipeStep(0L, 1L, "Step 1", 1), // Corrected constructor
            RecipeStep(0L, 1L, "Step 2", 2)  // Corrected constructor
        )
        coEvery { recipeDao.upsert(recipeSteps) } just runs // Corrected DAO method name

        // When
        recipeRepository.upsertRecipeSteps(recipeSteps)

        // Then
        coVerify { recipeDao.upsert(recipeSteps) } // Corrected DAO method name
    }

    @Test
    fun `getRecipeWithSteps should return recipe with steps from dao`() = runTest {
        // Given
        val recipeId = 1L
        val recipe = Recipe(recipeId, 30, 60, "Test Recipe", "Some tips")
        val steps = listOf(
            RecipeStep(1L, recipeId, "Step 1", 1), // Corrected constructor
            RecipeStep(2L, recipeId, "Step 2", 2)  // Corrected constructor
        )
        val recipeWithSteps = RecipeWithSteps(recipe, steps)
        coEvery { recipeDao.getRecipeWithSteps(recipeId) } returns recipeWithSteps // Corrected mock for suspend fun

        // When
        val result = recipeRepository.getRecipeWithSteps(recipeId) // Call suspend fun directly

        // Then
        result shouldBe recipeWithSteps
        coVerify { recipeDao.getRecipeWithSteps(recipeId) } // coVerify for suspend fun
    }

    @Test
    fun `deleteRecipeStepsByIds should call dao to delete recipe steps by ids`() = runTest {
        // Given
        val ids = listOf(1L, 2L)
        coEvery { recipeDao.deleteRecipeStepsByIds(ids) } returns Unit

        // When
        recipeRepository.deleteRecipeStepsByIds(ids)

        // Then
        coVerify { recipeDao.deleteRecipeStepsByIds(ids) }
    }
}
