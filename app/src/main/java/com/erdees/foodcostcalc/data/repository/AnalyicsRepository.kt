package com.erdees.foodcostcalc.data.repository

import android.os.Bundle
import com.erdees.foodcostcalc.BuildConfig
import com.erdees.foodcostcalc.utils.Constants
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

    fun setUserProperty(name: String, value: String?)

    fun logEvent(event: String)

    fun logEvent(event: String, bundle: Bundle?)

    fun logException(exception: Throwable, bundle: Bundle?)
}

class AnalyticsRepositoryImpl(private val firebaseAnalytics: FirebaseAnalytics) :
    AnalyticsRepository {

    override fun setUserProperty(name: String, value: String?) {
        if (!BuildConfig.DEBUG) {
            firebaseAnalytics.setUserProperty(name, value)
        } else {
            Timber.d("Set user property: $name = $value")
        }
    }

    override fun logEvent(event: String) {
        this.logEvent(event, null)
    }

    override fun logEvent(event: String, bundle: Bundle?) {
        if (!BuildConfig.DEBUG) {
            firebaseAnalytics.logEvent(event, bundle)
        } else {
            Timber.d("Event: $event, Bundle: $bundle")
        }
    }

    override fun logException(
        exception: Throwable,
        bundle: Bundle?
    ) {
        bundle?.apply {
            putString(Constants.Analytics.Exceptions.MESSAGE, exception.message)
        }
        if (!BuildConfig.DEBUG) {
            firebaseAnalytics.logEvent(Constants.Analytics.Exceptions.EVENT, bundle)
        } else {
            Timber.d("Exception: $exception, Bundle: $bundle")
        }
    }
}