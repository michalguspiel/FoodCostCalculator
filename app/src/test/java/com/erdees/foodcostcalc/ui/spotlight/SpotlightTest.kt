package com.erdees.foodcostcalc.ui.spotlight

import androidx.compose.ui.geometry.Rect
import com.erdees.foodcostcalc.data.repository.AnalyticsRepository
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

@ExperimentalCoroutinesApi
class SpotlightTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var testScope: TestScope

    private lateinit var spotlight: Spotlight

    private val analyticsRepository = mockk<AnalyticsRepository>(relaxed = true)

    @Before
    fun setUp() {
        testScope = TestScope(testDispatcher)
        spotlight = Spotlight(testScope)
        startKoin {
            modules(
                module {
                    single { analyticsRepository }
                }
            )
        }
    }

    @After
    fun tearDown() {
        clearAllMocks()
        stopKoin()
    }

    @Test
    fun `initial state is inactive`() = testScope.runTest {
        spotlight.isActive shouldBe false
        spotlight.currentTarget shouldBe null
    }

    @Test
    fun `start with empty list does not activate spotlight`() = testScope.runTest {
        spotlight.start(emptyList())
        spotlight.isActive shouldBe false
    }

    @Test
    fun `start with targets activates spotlight and sets first target`() = testScope.runTest {
        val target1 = mockk<SpotlightTarget>(relaxed = true)
        coEvery { target1.scrollToElement?.invoke() } coAnswers {}
        val target2 = mockk<SpotlightTarget>(relaxed = true)
        val targets = listOf(target1, target2)

        spotlight.start(targets)
        advanceUntilIdle()

        spotlight.isActive shouldBe true
        spotlight.currentTarget shouldBe target1
        coVerify(exactly = 1) { target1.scrollToElement?.invoke() }
    }

    @Test
    fun `next moves to next target and scrolls to it`() = testScope.runTest {
        val target1 = mockk<SpotlightTarget>(relaxed = true)
        every { target1.scrollToElement } returns null
        val target2 = mockk<SpotlightTarget>(relaxed = true)
        coEvery { target2.scrollToElement?.invoke() } coAnswers {}
        val targets = listOf(target1, target2)
        spotlight.start(targets)
        advanceUntilIdle()
        spotlight.next()
        advanceUntilIdle()
        spotlight.isActive shouldBe true
        spotlight.currentTarget shouldBe target2
        verify(exactly = 1) { target1.onClickAction?.invoke() }
        coVerify(exactly = 1) { target2.scrollToElement?.invoke() }
    }

    @Test
    fun `next on last target deactivates spotlight`() = testScope.runTest {
        val target1 = mockk<SpotlightTarget>(relaxed = true)
        val target2 = mockk<SpotlightTarget>(relaxed = true)
        val targets = listOf(target1, target2)
        spotlight.start(targets)

        spotlight.next() // now on target2
        spotlight.next() // should deactivate

        spotlight.isActive shouldBe false
        spotlight.currentTarget shouldBe null
        verify(exactly = 1) { target2.onClickAction?.invoke() }
    }

    val target1Info = 101
    val target2Info = 102

    @Test
    fun `updateTarget does nothing if target not found`() = testScope.runTest {
        val target1 = SpotlightTarget(order = 1, info = target1Info, hasNextButton = false)
        spotlight.start(listOf(target1))
        val initialTargets = spotlight.targets

        val unknownTarget = SpotlightTarget(order = 2, info = target2Info, hasNextButton = false)
        spotlight.updateTarget(unknownTarget)

        spotlight.targets shouldBe initialTargets
    }

    @Test
    fun `updateTarget updates rect`() = testScope.runTest {
        val initialRect = mockk<Rect>()
        val newRect = mockk<Rect>()
        val target1 =
            SpotlightTarget(order = 1, target1Info, hasNextButton = false, rect = initialRect)
        spotlight.start(listOf(target1))

        val updatedTarget = target1.copy(rect = newRect)
        spotlight.updateTarget(updatedTarget)

        spotlight.targets.first().rect shouldBe newRect
    }

    @Test
    fun `updateTarget updates onClickAction when it was null`() = testScope.runTest {
        val target1 =
            SpotlightTarget(order = 1, target1Info, hasNextButton = false, onClickAction = null)
        spotlight.start(listOf(target1))
        val newAction = mockk<() -> Unit>()

        val updatedTarget = target1.copy(onClickAction = newAction)
        spotlight.updateTarget(updatedTarget)

        spotlight.targets.first().onClickAction shouldBe newAction
    }

    @Test
    fun `updateTarget updates scrollToElement when it was null`() = testScope.runTest {
        val target1 =
            SpotlightTarget(order = 1, target1Info, hasNextButton = false, scrollToElement = null)
        spotlight.start(listOf(target1))
        val newScrollAction = mockk<suspend () -> Unit>(relaxed = true)

        val updatedTarget = target1.copy(scrollToElement = newScrollAction)
        spotlight.updateTarget(updatedTarget)

        spotlight.targets.first().scrollToElement shouldBe newScrollAction
    }

    @Test
    fun `updateTarget does not update with identical properties`() = testScope.runTest {
        val rect = mockk<Rect>()
        val action = mockk<() -> Unit>()
        val target1 =
            SpotlightTarget(order = 1, target1Info, hasNextButton = false, rect = rect, onClickAction = action)
        spotlight.start(listOf(target1))
        val initialTargets = spotlight.targets

        val sameTarget = target1.copy()
        spotlight.updateTarget(sameTarget)

        spotlight.targets shouldBe initialTargets
    }

    @Test
    fun `updateTarget preserves existing properties`() = testScope.runTest {
        val rect = mockk<Rect>()
        val newRect = mockk<Rect>()
        val action = mockk<() -> Unit>()
        val target1 = SpotlightTarget(order = 1, info = target1Info, hasNextButton = false, rect = rect, onClickAction = action)
        spotlight.start(listOf(target1))

        val updatedTarget = target1.copy(rect = newRect)
        spotlight.updateTarget(updatedTarget)

        val resultTarget = spotlight.targets.first()
        resultTarget.rect shouldBe newRect
        resultTarget.onClickAction shouldBe action
    }

    @Test
    fun `updateTarget updates multiple properties`() = testScope.runTest {
        val initialRect = mockk<Rect>()
        val newRect = mockk<Rect>()
        val newAction = mockk<() -> Unit>()
        val newScrollAction = mockk<suspend () -> Unit>(relaxed = true)

        val target1 = SpotlightTarget(
            order = 1,
            info = target1Info,
            hasNextButton = false,
            rect = initialRect,
            onClickAction = null,
            scrollToElement = null
        )
        spotlight.start(listOf(target1))

        val updatedTarget = target1.copy(
            rect = newRect,
            onClickAction = newAction,
            scrollToElement = newScrollAction
        )
        spotlight.updateTarget(updatedTarget)

        val resultTarget = spotlight.targets.first()
        resultTarget.rect shouldBe newRect
        resultTarget.onClickAction shouldBe newAction
        resultTarget.scrollToElement shouldBe newScrollAction
    }
}
