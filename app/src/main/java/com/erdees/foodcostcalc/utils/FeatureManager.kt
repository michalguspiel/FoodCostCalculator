package com.erdees.foodcostcalc.utils

import java.time.LocalDate
import java.time.Month
import java.time.ZoneId

interface FeatureManager {
    fun isFeatureEnabled(feature: Feature): Boolean
}

enum class Feature(val cutOffDate: LocalDate) {
    HIDE_PRODUCT_TAX_BY_DEFAULT(LocalDate.of(2025, Month.JUNE, 1)),
    HIDE_HALF_PRODUCTS_BY_DEFAULT(LocalDate.of(2025, Month.JUNE, 1)),
    SET_DEFAULTS_PROMPT(LocalDate.of(2025, Month.JULY, 20)),
}

@Suppress("MagicNumber")
class FeatureManagerImpl(
    private val firstInstallTime: Long?,
) : FeatureManager {
    override fun isFeatureEnabled(feature: Feature): Boolean {
        return feature.isEnabledByCutOffTime()
    }

    private fun Feature.isEnabledByCutOffTime(): Boolean {
        if (firstInstallTime == null) {
            // Let's simply assume the feature is disabled if we can't determine the install time.
            return false
        }
        return firstInstallTime >
                this.cutOffDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
    }
}