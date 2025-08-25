package com.erdees.foodcostcalc.utils.di

import android.content.pm.PackageManager
import com.erdees.foodcostcalc.domain.manager.EntitlementManager
import com.erdees.foodcostcalc.utils.FeatureManager
import com.erdees.foodcostcalc.utils.FeatureManagerImpl
import com.erdees.foodcostcalc.utils.MyDispatchers
import com.erdees.foodcostcalc.utils.MyDispatchersImpl
import com.erdees.foodcostcalc.utils.billing.PremiumUtil
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import timber.log.Timber

val utilModule = module {
    single<PremiumUtil> { PremiumUtil(get()) }
    single<MyDispatchers> { MyDispatchersImpl() }
    single<FeatureManager> {
        val firstInstallTime: Long? = try {
            val packageInfo =
                androidContext().packageManager.getPackageInfo(androidContext().packageName, 0)
            packageInfo.firstInstallTime
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.e(e, "Could not get package info to determine first install time.")
            null
        }
        FeatureManagerImpl(firstInstallTime = firstInstallTime)
    }
    single<EntitlementManager> {
        EntitlementManager(
            preferences = get(),
            dishRepository = get(),
            halfProductRepository = get(),
            featureCutOffManager = get()
        )
    }
}
