package com.erdees.foodcostcalc.utils

import io.kotest.matchers.shouldBe
import org.junit.Test
import java.time.LocalDate
import java.time.Month
import java.time.ZoneId

class FeatureManagerImplTest {

    // Helper to convert date to millis since epoch for consistency in tests
    private fun getMillis(month: Month, day: Int): Long {
        return LocalDate
            .of(2025, month, day)
            .atStartOfDay(ZoneId.of("UTC"))
            .toInstant()
            .toEpochMilli()
    }

    @Test
    fun `GIVEN firstInstallTime is after isFeatureEnabled returns true`() {
        val firstInstallTime = getMillis(Month.JUNE, 10)
        val checker: FeatureManager = FeatureManagerImpl(
            firstInstallTime = firstInstallTime,
        )
        val result = checker.isFeatureEnabled(Feature.HIDE_PRODUCT_TAX_BY_DEFAULT)
        result shouldBe true
    }

    @Test
    fun `GIVEN firstInstallTime is before isFeatureEnabled returns false`() {
        val firstInstallTime = getMillis(Month.MAY, 31)
        val checker: FeatureManager = FeatureManagerImpl(firstInstallTime = firstInstallTime)
        val result = checker.isFeatureEnabled(Feature.HIDE_PRODUCT_TAX_BY_DEFAULT)
        result shouldBe false
    }

    @Test
    fun `firstInstallTime is Long MAX VALUE`() {
        val firstInstallTime = Long.MAX_VALUE
        val checker: FeatureManager = FeatureManagerImpl(
            firstInstallTime = firstInstallTime,
        )
        val result = checker.isFeatureEnabled(Feature.HIDE_PRODUCT_TAX_BY_DEFAULT)
        result shouldBe true
    }

    @Test
    fun `firstInstallTime is Long MIN VALUE`() {
        val firstInstallTime = Long.MIN_VALUE
        val checker: FeatureManager = FeatureManagerImpl(
            firstInstallTime = firstInstallTime,
        )
        val result = checker.isFeatureEnabled(Feature.HIDE_PRODUCT_TAX_BY_DEFAULT)
        result shouldBe false
    }

    @Test
    fun `GIVEN firstInstallTime is before grandfathered cutoff isGrandfatheredUser returns true`() {
        // November 1, 2024 (before December 1, 2024 cutoff)
        val firstInstallTime = LocalDate
            .of(2024, Month.NOVEMBER, 1)
            .atStartOfDay(ZoneId.of("UTC"))
            .toInstant()
            .toEpochMilli()
        val checker: FeatureManager = FeatureManagerImpl(firstInstallTime = firstInstallTime)
        val result = checker.isGrandfatheredUser()
        result shouldBe true
    }

    @Test
    fun `GIVEN firstInstallTime is after grandfathered cutoff isGrandfatheredUser returns false`() {
        // December 15, 2024 (after December 1, 2024 cutoff)
        val firstInstallTime = LocalDate
            .of(2024, Month.DECEMBER, 15)
            .atStartOfDay(ZoneId.of("UTC"))
            .toInstant()
            .toEpochMilli()
        val checker: FeatureManager = FeatureManagerImpl(firstInstallTime = firstInstallTime)
        val result = checker.isGrandfatheredUser()
        result shouldBe false
    }

    @Test
    fun `GIVEN firstInstallTime is null isGrandfatheredUser returns false`() {
        val checker: FeatureManager = FeatureManagerImpl(firstInstallTime = null)
        val result = checker.isGrandfatheredUser()
        result shouldBe false
    }
}