package com.erdees.foodcostcalc.data.repository

import android.os.Bundle
import com.erdees.foodcostcalc.BuildConfig
import com.google.firebase.analytics.FirebaseAnalytics
import timber.log.Timber

/**
 * Interface for the Analytics Repository.
 *
 * This interface defines the contract for logging events to Firebase Analytics.
 *
 * The main benefit of using this repository is to prevent logging events in debug builds
 * with a single if-clause, ensuring that analytics data is only collected in production.
 *
 * @see AnalyticsRepositoryImpl for the implementation details.
 */
interface AnalyticsRepository {
    fun logEvent(event: String, bundle: Bundle?)
}

class AnalyticsRepositoryImpl(private val firebaseAnalytics: FirebaseAnalytics) :
    AnalyticsRepository {
    override fun logEvent(event: String, bundle: Bundle?) {
        if (!BuildConfig.DEBUG) {
            firebaseAnalytics.logEvent(event, bundle)
        } else {
            Timber.d("Event: $event, Bundle: $bundle")
        }
    }
}