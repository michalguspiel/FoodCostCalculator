package com.erdees.foodcostcalc.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.QueryPurchasesParams
import com.erdees.foodcostcalc.data.Preferences
import com.erdees.foodcostcalc.ui.screens.FCCHostScreen
import com.erdees.foodcostcalc.ui.theme.FCCTheme
import com.erdees.foodcostcalc.utils.billing.PremiumUtil
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject
import timber.log.Timber

class FCCActivity : AppCompatActivity() {

    private val preferences: Preferences by inject(Preferences::class.java)
    private val premiumUtil: PremiumUtil by inject(PremiumUtil::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()
        Timber.i("onCreate")

        premiumUtil.billingClient = BillingClient.newBuilder(this)
            .setListener(premiumUtil.purchaseUpdateListener)
            .enablePendingPurchases(
                PendingPurchasesParams
                    .newBuilder()
                    .enableOneTimeProducts()
                    .build()
            )
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            premiumUtil.billingSetup()
        }

        CoroutineScope(Dispatchers.IO).launch {
            val testDevices = listOf(
                "3C07BBF025D37C2860EE53088321FCB2",
                "6D82FB226E12482C4555652147F98C12"
            )
            val adsRequestConfiguration = RequestConfiguration.Builder()
                .setTestDeviceIds(testDevices)
                .build()
            MobileAds.setRequestConfiguration(adsRequestConfiguration)
            MobileAds.initialize(this@FCCActivity) {
                // Initialization complete. It is now safe to show ads.
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