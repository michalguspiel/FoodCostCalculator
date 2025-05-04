package com.erdees.foodcostcalc.data

import android.content.Context
import android.icu.util.Currency
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.erdees.foodcostcalc.ext.dataStore
import com.erdees.foodcostcalc.utils.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.util.Locale

interface Preferences {
    val defaultMargin: Flow<String>
    suspend fun setDefaultMargin(margin: String)

    val defaultTax: Flow<String>
    suspend fun setDefaultTax(tax: String)

    val defaultCurrencyCode: Flow<String?>
    val currency: Flow<Currency?>
    suspend fun setDefaultCurrencyCode(code: String?)

    fun userHasActiveSubscription(): Flow<Boolean>
    suspend fun setUserHasActiveSubscription(value: Boolean)

    val metricUsed: Flow<Boolean>
    suspend fun setMetricUsed(value: Boolean)

    val imperialUsed: Flow<Boolean>
    suspend fun setImperialUsed(value: Boolean)
}


class PreferencesImpl(private val context: Context) : Preferences {

    private val defaultLocale: Locale = Locale.getDefault()
    private val localeDefaultCurrencyCode: String? =
        Currency.getInstance(defaultLocale)?.currencyCode

    // Preference Keys
    private object Keys {
        val DEFAULT_MARGIN = stringPreferencesKey(Constants.MARGIN)
        val DEFAULT_TAX = stringPreferencesKey(Constants.TAX)
        val CURRENCY_CODE = stringPreferencesKey(Constants.PREFERRED_CURRENCY_CODE)
        val SUBSCRIPTION_STATE = booleanPreferencesKey(Constants.SUBSCRIPTION_STATE)
        val METRIC = booleanPreferencesKey(Constants.METRIC)
        val IMPERIAL = booleanPreferencesKey(Constants.IMPERIAL)
    }

    override val defaultCurrencyCode: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[Keys.CURRENCY_CODE] ?: localeDefaultCurrencyCode
    }

    override val currency: Flow<Currency?> = defaultCurrencyCode.map { code ->
        code?.let {
            runCatching {
                Currency.getInstance(it)
            }.getOrNull()
        }
    }

    override suspend fun setDefaultCurrencyCode(code: String?) {
        code?.let {
            context.dataStore.edit { prefs ->
                prefs[Keys.CURRENCY_CODE] = it
            }
        }
    }

    override val defaultMargin: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.DEFAULT_MARGIN] ?: Constants.BASIC_MARGIN.toString()
    }

    override suspend fun setDefaultMargin(margin: String) {
        context.dataStore.edit { prefs -> prefs[Keys.DEFAULT_MARGIN] = margin }
    }

    override val defaultTax: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.DEFAULT_TAX] ?: Constants.BASIC_TAX.toString()
    }

    override suspend fun setDefaultTax(tax: String) {
        context.dataStore.edit { prefs -> prefs[Keys.DEFAULT_TAX] = tax }
    }

    override fun userHasActiveSubscription(): Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.SUBSCRIPTION_STATE] == true
    }

    override suspend fun setUserHasActiveSubscription(value: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SUBSCRIPTION_STATE] = value
        }
        Timber.i("Subscription state set to $value")
    }

    override val metricUsed: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[Keys.METRIC] != false }

    override suspend fun setMetricUsed(value: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.METRIC] = value }
    }

    override val imperialUsed: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[Keys.IMPERIAL] == true }


    override suspend fun setImperialUsed(value: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.IMPERIAL] = value }
    }
}
