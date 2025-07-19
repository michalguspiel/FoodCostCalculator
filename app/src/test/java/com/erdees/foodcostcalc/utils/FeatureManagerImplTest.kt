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
}