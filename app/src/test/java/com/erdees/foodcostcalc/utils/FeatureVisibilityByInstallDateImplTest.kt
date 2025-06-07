package com.erdees.foodcostcalc.utils

import io.kotest.matchers.shouldBe
import org.junit.Test
import java.time.LocalDate
import java.time.Month
import java.time.ZoneId

class FeatureVisibilityByInstallDateImplTest {

    // Helper to convert date to millis since epoch for consistency in tests
    private fun getMillis(month: Month, day: Int): Long {
        return LocalDate
            .of(2025, month, day)
            .atStartOfDay(ZoneId.of("UTC"))
            .toInstant()
            .toEpochMilli()
    }
    private val featureHiddenByDefaultCutoffTimeMs = LocalDate.of(2025, Month.JUNE, 1)

    @Test
    fun `Pre cutoff install time assumeVisibleOnError true`() {
        // Given firstInstallTime is before featureHiddenByDefaultCutoff
        val firstInstallTime = getMillis(Month.MAY, 31)
        val checker: FeatureVisibilityByInstallDate = FeatureVisibilityByInstallDateImpl(
            firstInstallTime = firstInstallTime,
            cutOffLocalDate = featureHiddenByDefaultCutoffTimeMs
        )
        // When isDefaultVisibleForPreCutoffUser is called
        val result = checker.isDefaultVisibleForPreCutoffUser(true)
        // Then it should return true.
        result shouldBe true
    }

    @Test
    fun `Pre cutoff install time assumeVisibleOnError false`() {
        // Given firstInstallTime is before featureHiddenByDefaultCutoff
        val firstInstallTime = getMillis(Month.MAY, 31)
        val checker: FeatureVisibilityByInstallDate = FeatureVisibilityByInstallDateImpl(
            firstInstallTime = firstInstallTime,
            cutOffLocalDate = featureHiddenByDefaultCutoffTimeMs
        )
        // When isDefaultVisibleForPreCutoffUser is called
        val result = checker.isDefaultVisibleForPreCutoffUser(false)
        // Then it should return true.
        result shouldBe true
    }

    @Test
    fun `Post cutoff install time assumeVisibleOnError true`() {
        // Given firstInstallTime is before featureHiddenByDefaultCutoff
        val firstInstallTime = getMillis(Month.JUNE, 10)
        val checker: FeatureVisibilityByInstallDate = FeatureVisibilityByInstallDateImpl(
            firstInstallTime = firstInstallTime,
            cutOffLocalDate = featureHiddenByDefaultCutoffTimeMs
        )
        // When isDefaultVisibleForPreCutoffUser is called
        val result = checker.isDefaultVisibleForPreCutoffUser(true)
        // Then it should return false.
        result shouldBe false
    }

    @Test
    fun `Post cutoff install time assumeVisibleOnError false`() {
        // Given firstInstallTime is before featureHiddenByDefaultCutoff
        val firstInstallTime = getMillis(Month.JUNE, 10)
        val checker: FeatureVisibilityByInstallDate = FeatureVisibilityByInstallDateImpl(
            firstInstallTime = firstInstallTime,
            cutOffLocalDate = featureHiddenByDefaultCutoffTimeMs
        )
        // When isDefaultVisibleForPreCutoffUser is called
        val result = checker.isDefaultVisibleForPreCutoffUser(false)
        // Then it should return false.
        result shouldBe false
    }

    @Test
    fun `install time null assumeVisibleOnErrortrue`() {
        // Given firstInstallTime is null
        val checker: FeatureVisibilityByInstallDate = FeatureVisibilityByInstallDateImpl(
            firstInstallTime = null,
            cutOffLocalDate = featureHiddenByDefaultCutoffTimeMs
        )
        // When isDefaultVisibleForPreCutoffUser is called
        val result = checker.isDefaultVisibleForPreCutoffUser(true)
        // Then it returns assumeVisibleOnError
        result shouldBe true
    }

    @Test
    fun `install time null assumeVisibleOnError false`() {
        // Given firstInstallTime is null
        val checker: FeatureVisibilityByInstallDate = FeatureVisibilityByInstallDateImpl(
            firstInstallTime = null,
            cutOffLocalDate = featureHiddenByDefaultCutoffTimeMs
        )
        // When isDefaultVisibleForPreCutoffUser is called
        val result = checker.isDefaultVisibleForPreCutoffUser(false)
        // Then it returns assumeVisibleOnError
        result shouldBe false
    }

    @Test
    fun `firstInstallTime is Long MAX VALUE`() {
        // Given firstInstallTime is Long MAX VALUE
        val firstInstallTime = Long.MAX_VALUE
        val checker: FeatureVisibilityByInstallDate = FeatureVisibilityByInstallDateImpl(
            firstInstallTime = firstInstallTime,
            cutOffLocalDate = featureHiddenByDefaultCutoffTimeMs
        )
        // When isDefaultVisibleForPreCutoffUser is called
        val result = checker.isDefaultVisibleForPreCutoffUser(true)
        // Then it should return false.
        result shouldBe false
    }

    @Test
    fun `firstInstallTime is Long MIN VALUE`() {
        // Given firstInstallTime is before featureHiddenByDefaultCutoff
        val firstInstallTime = Long.MIN_VALUE
        val checker: FeatureVisibilityByInstallDate = FeatureVisibilityByInstallDateImpl(
            firstInstallTime = firstInstallTime,
            cutOffLocalDate = featureHiddenByDefaultCutoffTimeMs
        )
        // When isDefaultVisibleForPreCutoffUser is called
        val result = checker.isDefaultVisibleForPreCutoffUser(true)
        // Then it should return true.
        result shouldBe true
    }
}