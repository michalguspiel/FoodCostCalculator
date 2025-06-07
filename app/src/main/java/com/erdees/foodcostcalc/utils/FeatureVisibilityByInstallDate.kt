package com.erdees.foodcostcalc.utils

import java.time.LocalDate
import java.time.Month
import java.time.ZoneId

/**
 * Interface to determine feature visibility based on an install date
 * relative to a cutoff date.
 */
interface FeatureVisibilityByInstallDate {
    /**
     * Checks if a feature should be visible by default for a user
     * who installed the app before a predefined cutoff time.
     *
     * @return true if the install time is before the cutoff, false otherwise.
     */
    fun isDefaultVisibleForPreCutoffUser(assumeVisibleOnError: Boolean): Boolean
}

@Suppress("MagicNumber")
class FeatureVisibilityByInstallDateImpl(
    private val firstInstallTime: Long?,
    private val cutOffLocalDate: LocalDate = LocalDate.of(2025, Month.JUNE, 1)
) : FeatureVisibilityByInstallDate {

    private val featureHiddenByDefaultCutoff: Long by lazy {
        cutOffLocalDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
    }

    override fun isDefaultVisibleForPreCutoffUser(
        assumeVisibleOnError: Boolean
    ): Boolean {
        return if (firstInstallTime == null) {
            assumeVisibleOnError
        } else {
            firstInstallTime < featureHiddenByDefaultCutoff
        }
    }
}