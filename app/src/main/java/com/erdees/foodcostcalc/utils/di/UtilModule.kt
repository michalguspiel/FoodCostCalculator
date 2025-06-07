package com.erdees.foodcostcalc.utils.di

import android.content.pm.PackageManager
import com.erdees.foodcostcalc.utils.FeatureVisibilityByInstallDate
import com.erdees.foodcostcalc.utils.FeatureVisibilityByInstallDateImpl
import com.erdees.foodcostcalc.utils.MyDispatchers
import com.erdees.foodcostcalc.utils.MyDispatchersImpl
import com.erdees.foodcostcalc.utils.billing.PremiumUtil
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import timber.log.Timber

val utilModule = module {
    single<PremiumUtil> { PremiumUtil(get()) }
    single<MyDispatchers> { MyDispatchersImpl() }
    single<FeatureVisibilityByInstallDate> {
        val firstInstallTime: Long? = try {
            val packageInfo =
                androidContext().packageManager.getPackageInfo(androidContext().packageName, 0)
            packageInfo.firstInstallTime
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.e(e, "Could not get package info to determine first install time.")
            null
        }
        FeatureVisibilityByInstallDateImpl(firstInstallTime = firstInstallTime)
    }
}
