package com.erdees.foodcostcalc.utils

import java.time.LocalDate
import java.time.Month
import java.time.ZoneId

interface FeatureManager {
    fun isFeatureEnabled(feature: Feature): Boolean
    fun isGrandfatheredUser(): Boolean
}

enum class Feature(val cutOffDate: LocalDate) {
    HIDE_PRODUCT_TAX_BY_DEFAULT(LocalDate.of(2025, Month.JUNE, 1)),
    HIDE_HALF_PRODUCTS_BY_DEFAULT(LocalDate.of(2025, Month.JUNE, 1)),
    SET_DEFAULTS_PROMPT(LocalDate.of(2025, Month.JULY, 20)),
}

/**
 * Cutoff date for grandfathered users. Users who installed before this date
 * get free access to premium features.
 */
private val GRANDFATHERED_USER_CUTOFF_DATE = LocalDate.of(2025, Month.OCTOBER, 1)

@Suppress("MagicNumber")
class FeatureManagerImpl(
    private val firstInstallTime: Long?,
) : FeatureManager {
    override fun isFeatureEnabled(feature: Feature): Boolean {
        return feature.isEnabledByCutOffTime()
    }

    override fun isGrandfatheredUser(): Boolean {
        if (firstInstallTime == null) {
            // If we can't determine the install time, assume they're not grandfathered
            return false
        }
        return firstInstallTime < 
                GRANDFATHERED_USER_CUTOFF_DATE.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
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