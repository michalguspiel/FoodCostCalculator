package com.erdees.foodcostcalc.domain.manager

import com.erdees.foodcostcalc.data.Preferences
import com.erdees.foodcostcalc.data.repository.DishRepository
import com.erdees.foodcostcalc.data.repository.HalfProductRepository
import com.erdees.foodcostcalc.domain.model.GatedFeature
import com.erdees.foodcostcalc.utils.FeatureManager
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class EntitlementManagerTest {

    private val userRepository = mockk<Preferences>()
    private val dishRepository = mockk<DishRepository>()
    private val halfProductRepository = mockk<HalfProductRepository>()
    private val featureCutOffManager = mockk<FeatureManager>()

    private val entitlementManager = EntitlementManager(
        preferences = userRepository,
        dishRepository = dishRepository,
        halfProductRepository = halfProductRepository,
        featureCutOffManager = featureCutOffManager
    )

    @Test
    fun `canCreateDish returns true when user has active subscription`() = runTest {
        // Given
        every { userRepository.userHasActiveSubscription() } returns flowOf(true)

        // When
        val result = entitlementManager.canCreateDish()

        // Then
        result shouldBe true
    }

    @Test
    fun `canCreateDish returns true when user is grandfathered`() = runTest {
        // Given
        every { userRepository.userHasActiveSubscription() } returns flowOf(false)
        every { featureCutOffManager.isGrandfatheredUser() } returns true

        // When
        val result = entitlementManager.canCreateDish()

        // Then
        result shouldBe true
    }

    @Test
    fun `canCreateDish returns true when user is under free limit`() = runTest {
        // Given
        every { userRepository.userHasActiveSubscription() } returns flowOf(false)
        every { featureCutOffManager.isGrandfatheredUser() } returns false
        coEvery { dishRepository.getDishCount() } returns 15

        // When
        val result = entitlementManager.canCreateDish()

        // Then
        result shouldBe true
    }

    @Test
    fun `canCreateDish returns false when user is at free limit`() = runTest {
        // Given
        every { userRepository.userHasActiveSubscription() } returns flowOf(false)
        every { featureCutOffManager.isGrandfatheredUser() } returns false
        coEvery { dishRepository.getDishCount() } returns 20

        // When
        val result = entitlementManager.canCreateDish()

        // Then
        result shouldBe false
    }

    @Test
    fun `canCreateDish returns false when user exceeds free limit`() = runTest {
        // Given
        every { userRepository.userHasActiveSubscription() } returns flowOf(false)
        every { featureCutOffManager.isGrandfatheredUser() } returns false
        coEvery { dishRepository.getDishCount() } returns 25

        // When
        val result = entitlementManager.canCreateDish()

        // Then
        result shouldBe false
    }

    @Test
    fun `canCreateHalfProduct returns true when user has active subscription`() = runTest {
        // Given
        every { userRepository.userHasActiveSubscription() } returns flowOf(true)

        // When
        val result = entitlementManager.canCreateHalfProduct()

        // Then
        result shouldBe true
    }

    @Test
    fun `canCreateHalfProduct returns true when user is grandfathered`() = runTest {
        // Given
        every { userRepository.userHasActiveSubscription() } returns flowOf(false)
        every { featureCutOffManager.isGrandfatheredUser() } returns true

        // When
        val result = entitlementManager.canCreateHalfProduct()

        // Then
        result shouldBe true
    }

    @Test
    fun `canCreateHalfProduct returns true when user is under free limit`() = runTest {
        // Given
        every { userRepository.userHasActiveSubscription() } returns flowOf(false)
        every { featureCutOffManager.isGrandfatheredUser() } returns false
        coEvery { halfProductRepository.getHalfProductCount() } returns 1

        // When
        val result = entitlementManager.canCreateHalfProduct()

        // Then
        result shouldBe true
    }

    @Test
    fun `canCreateHalfProduct returns false when user is at free limit`() = runTest {
        // Given
        every { userRepository.userHasActiveSubscription() } returns flowOf(false)
        every { featureCutOffManager.isGrandfatheredUser() } returns false
        coEvery { halfProductRepository.getHalfProductCount() } returns 2

        // When
        val result = entitlementManager.canCreateHalfProduct()

        // Then
        result shouldBe false
    }

    @Test
    fun `canCreateHalfProduct returns false when user exceeds free limit`() = runTest {
        // Given
        every { userRepository.userHasActiveSubscription() } returns flowOf(false)
        every { featureCutOffManager.isGrandfatheredUser() } returns false
        coEvery { halfProductRepository.getHalfProductCount() } returns 5

        // When
        val result = entitlementManager.canCreateHalfProduct()

        // Then
        result shouldBe false
    }

    @Test
    fun `isFeatureUnlocked returns flow with true when user has subscription`() = runTest {
        // Given
        every { userRepository.userHasActiveSubscription() } returns flowOf(true)

        // When
        val result = entitlementManager.isFeatureUnlocked(GatedFeature.CloudSync).first()

        // Then
        result shouldBe true
    }

    @Test
    fun `isFeatureUnlocked returns flow with false when user has no subscription`() = runTest {
        // Given
        every { userRepository.userHasActiveSubscription() } returns flowOf(false)

        // When
        val result = entitlementManager.isFeatureUnlocked(GatedFeature.PdfExport).first()

        // Then
        result shouldBe false
    }

    @Test
    fun `isFeatureUnlocked works with all gated features`() = runTest {
        // Given
        every { userRepository.userHasActiveSubscription() } returns flowOf(true)

        // When & Then
        entitlementManager.isFeatureUnlocked(GatedFeature.CloudSync).first() shouldBe true
        entitlementManager.isFeatureUnlocked(GatedFeature.PdfExport).first() shouldBe true
        entitlementManager.isFeatureUnlocked(GatedFeature.UnlimitedRecipes).first() shouldBe true
    }
}