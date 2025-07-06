package com.erdees.foodcostcalc.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.QueryPurchasesParams
import com.erdees.foodcostcalc.BuildConfig
import com.erdees.foodcostcalc.data.Preferences
import com.erdees.foodcostcalc.domain.model.onboarding.OnboardingState
import com.erdees.foodcostcalc.ui.screens.hostScreen.FCCHostScreen
import com.erdees.foodcostcalc.ui.theme.FCCTheme
import com.erdees.foodcostcalc.utils.billing.PremiumUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject
import timber.log.Timber

class FCCActivity : AppCompatActivity() {

    private val preferences: Preferences by inject(Preferences::class.java)
    private val premiumUtil: PremiumUtil by inject(PremiumUtil::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        Timber.i("onCreate")

        if (BuildConfig.DEBUG) {
            // Reset onboarding state in debug builds
            CoroutineScope(Dispatchers.IO).launch {
                preferences.setOnboardingState(OnboardingState.NOT_STARTED)
            }
        }

        setContent {
            FCCTheme {
                FCCHostScreen()
            }
        }
    }


    override fun onResume() {
        super.onResume()
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)

        premiumUtil.billingClient?.queryPurchasesAsync(params.build()) { billingResult, purchase ->
            Timber.i("queryPurchasesAsync(), responseCode: ${billingResult.responseCode}, purchase: $purchase")
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                // Note that queryPurchasesAsync() returns only active subscriptions.
                // FCC has only subscriptions, so we can safely assume that if the list is empty,
                // user does not have an active subscription.
                if (purchase.isEmpty()) {
                    // USER DOES NOT HAVE AN ACTIVE SUBSCRIPTION
                    lifecycleScope.launch(Dispatchers.IO) {
                        preferences.setUserHasActiveSubscription(false)
                    }
                } else {
                    // USER DOES HAVE AN ACTIVE SUBSCRIPTION
                    lifecycleScope.launch(Dispatchers.IO) {
                        preferences.setUserHasActiveSubscription(true)
                    }
                }
            }
        }
    }
}