package com.erdees.foodcostcalc.data

import android.content.Context
import android.icu.util.Currency
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.erdees.foodcostcalc.domain.model.onboarding.OnboardingState
import com.erdees.foodcostcalc.ext.dataStore
import com.erdees.foodcostcalc.utils.Constants
import com.erdees.foodcostcalc.utils.Feature
import com.erdees.foodcostcalc.utils.FeatureManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.java.KoinJavaComponent.inject
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

    val showHalfProducts: Flow<Boolean>
    suspend fun setShowHalfProducts(value: Boolean)

    val showProductTax: Flow<Boolean>
    suspend fun setShowProductTax(value: Boolean)

    val onboardingState: Flow<OnboardingState>
    suspend fun setOnboardingState(state: OnboardingState)

    val hasPromptedDefaultSettings: Flow<Boolean>
    suspend fun setHasPromptedDefaultSettings(value: Boolean)

    val hasSeenLoyaltyScreen: Flow<Boolean>
    suspend fun setHasSeenLoyaltyScreen(value: Boolean)
}


class PreferencesImpl(private val context: Context) : Preferences {

    private val featureManager by inject<FeatureManager>(
        FeatureManager::class.java
    )
    private val defaultLocale: Locale = Locale.getDefault()
    private val localeDefaultCurrencyCode: String? =
        Currency.getInstance(defaultLocale)?.currencyCode

    // Preference Keys
    private object Keys {
        val DEFAULT_MARGIN = stringPreferencesKey(Constants.Preferences.MARGIN)
        val DEFAULT_TAX = stringPreferencesKey(Constants.Preferences.TAX)
        val CURRENCY_CODE = stringPreferencesKey(Constants.Preferences.PREFERRED_CURRENCY_CODE)
        val SUBSCRIPTION_STATE = booleanPreferencesKey(Constants.Preferences.SUBSCRIPTION_STATE)
        val METRIC = booleanPreferencesKey(Constants.Preferences.METRIC)
        val IMPERIAL = booleanPreferencesKey(Constants.Preferences.IMPERIAL)
        val SHOW_HALF_PRODUCTS = booleanPreferencesKey(Constants.Preferences.SHOW_HALF_PRODUCTS)
        val SHOW_PRODUCT_TAX = booleanPreferencesKey(Constants.Preferences.SHOW_PRODUCT_TAX_PERCENT)
        val ONBOARDING_STATE = stringPreferencesKey(Constants.Preferences.ONBOARDING_STATE)
        val HAS_PROMPTED_DEFAULT_SETTINGS =
            booleanPreferencesKey(Constants.Preferences.HAS_PROMPTED_DEFAULT_SETTINGS)
        val HAS_SEEN_LOYALTY_SCREEN =
            booleanPreferencesKey(Constants.Preferences.HAS_SEEN_LOYALTY_SCREEN)
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

    // Return false by default if the feature is enabled,
    override val showHalfProducts: Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[Keys.SHOW_HALF_PRODUCTS]
                ?: !featureManager.isFeatureEnabled(Feature.HIDE_HALF_PRODUCTS_BY_DEFAULT)
        }

    override suspend fun setShowHalfProducts(value: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.SHOW_HALF_PRODUCTS] = value }
    }

    // Return false by default if the feature is enabled,
    override val showProductTax: Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[Keys.SHOW_PRODUCT_TAX]
                ?: !featureManager.isFeatureEnabled(Feature.HIDE_PRODUCT_TAX_BY_DEFAULT)
        }

    override suspend fun setShowProductTax(value: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.SHOW_PRODUCT_TAX] = value }
    }

    override val onboardingState: Flow<OnboardingState> = context.dataStore.data.map { prefs ->
        val stateName = prefs[Keys.ONBOARDING_STATE]
        Timber.i("Fetching onboarding state from preferences $stateName")
        stateName?.let { OnboardingState.valueOf(it) } ?: OnboardingState.NOT_STARTED
    }

    override suspend fun setOnboardingState(state: OnboardingState) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_STATE] = state.name
        }
    }

    // Return true by default if the feature is not enabled,
    // so that it acts as if the user has already been prompted
    override val hasPromptedDefaultSettings: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.HAS_PROMPTED_DEFAULT_SETTINGS]
            ?: !featureManager.isFeatureEnabled(Feature.SET_DEFAULTS_PROMPT)
    }

    override suspend fun setHasPromptedDefaultSettings(value: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.HAS_PROMPTED_DEFAULT_SETTINGS] = value }
    }

    override val hasSeenLoyaltyScreen: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.HAS_SEEN_LOYALTY_SCREEN] ?: false
    }

    override suspend fun setHasSeenLoyaltyScreen(value: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.HAS_SEEN_LOYALTY_SCREEN] = value }
    }
}
