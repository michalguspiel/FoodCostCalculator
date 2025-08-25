package com.erdees.foodcostcalc.ui

import android.app.Application
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.QueryPurchasesParams
import com.erdees.foodcostcalc.BuildConfig
import com.erdees.foodcostcalc.data.Preferences
import com.erdees.foodcostcalc.data.di.dbModule
import com.erdees.foodcostcalc.data.di.remoteDataModule
import com.erdees.foodcostcalc.data.di.repositoryModule
import com.erdees.foodcostcalc.domain.model.onboarding.OnboardingState
import com.erdees.foodcostcalc.domain.usecase.di.useCaseModule
import com.erdees.foodcostcalc.ui.di.appModule
import com.erdees.foodcostcalc.utils.billing.PremiumUtil
import com.erdees.foodcostcalc.utils.di.utilModule
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.initialize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.context.unloadKoinModules
import org.koin.java.KoinJavaComponent.get
import timber.log.Timber
import timber.log.Timber.DebugTree
import timber.log.Timber.Forest.plant


class MyApplication : Application() {

    // modules that need to be restarted after database recreation
    private val reloadableModules = listOf(dbModule, repositoryModule)
    private val otherModules = listOf(appModule, utilModule, remoteDataModule, useCaseModule)

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            plant(DebugTree())
            Timber.i("onCreate(), planted Timber tree.")
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build()
            )
            StrictMode.setVmPolicy(
                VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .build()
            )
            Timber.i("StrictMode set")
        }
        if (BuildConfig.DEBUG) {
            FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = false
        }

        Firebase.initialize(this)
        Firebase.appCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )
        Timber.i("Firebase App Check with Play Integrity Installed")

        startKoin()
        Timber.i("Koin Started!")

        initializeOnboardingState()
        initializeBilling()
        initializeAds()
    }

    private fun initializeOnboardingState() {
        if (BuildConfig.DEBUG) {
            // Reset onboarding state in debug builds, but only once at app launch
            val preferences: Preferences = get(Preferences::class.java)
            CoroutineScope(Dispatchers.IO).launch {
                // Only set it if not already set, to prevent inconsistencies
                    preferences.setOnboardingState(OnboardingState.NOT_STARTED)
                    Timber.i("Onboarding state initialized to NOT_STARTED")
            }
        }
    }

    private fun initializeBilling() {
        val premiumUtil: PremiumUtil = get(PremiumUtil::class.java)
        val preferences: Preferences = get(Preferences::class.java)
        premiumUtil.billingClient = BillingClient.newBuilder(this)
            .setListener(premiumUtil.purchaseUpdateListener)
            .enablePendingPurchases(
                PendingPurchasesParams
                    .newBuilder()
                    .enableOneTimeProducts()
                    .build()
            )
            .build()

        premiumUtil.billingClient?.let { client ->
            val params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)

            client.queryPurchasesAsync(params.build()) { billingResult, purchase ->
                Timber.i("queryPurchasesAsync(), responseCode: ${billingResult.responseCode}, purchase: $purchase")
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    if (purchase.isEmpty()) {
                        CoroutineScope(Dispatchers.IO).launch {
                            preferences.setCurrentActivePremiumPlan(null)
                        }
                    } else {
                        CoroutineScope(Dispatchers.IO).launch {
                            premiumUtil.setCurrentActivePremiumPlan(purchase)
                        }
                    }
                }
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            premiumUtil.billingSetup()
        }
    }

    private fun initializeAds() {
        CoroutineScope(Dispatchers.IO).launch {
            val testDevices = listOf(
                "3C07BBF025D37C2860EE53088321FCB2",
                "6D82FB226E12482C4555652147F98C12"
            )
            val adsRequestConfiguration = RequestConfiguration.Builder()
                .setTestDeviceIds(testDevices)
                .build()
            MobileAds.setRequestConfiguration(adsRequestConfiguration)
            MobileAds.initialize(this@MyApplication) {
                // Initialization complete. It is now safe to show ads.
            }
        }
    }

    private fun startKoin() {
        Timber.i("startKoin()")
        startKoin {
            // declare used Android context
            androidContext(this@MyApplication)
            // declare modules
            modules(
                reloadableModules + otherModules
            )
        }
    }

    /**
     * Necessary action after recreating database from online backup.
     * */
    fun restartDataModule() {
        Timber.i("restartDataModule()")
        unloadKoinModules(reloadableModules)
        loadKoinModules(reloadableModules)
    }
}