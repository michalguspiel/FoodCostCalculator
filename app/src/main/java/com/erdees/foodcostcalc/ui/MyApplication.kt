package com.erdees.foodcostcalc.ui

import android.app.Application
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import com.erdees.foodcostcalc.BuildConfig
import com.erdees.foodcostcalc.data.di.dbModule
import com.erdees.foodcostcalc.data.di.remoteDataModule
import com.erdees.foodcostcalc.data.di.repositoryModule
import com.erdees.foodcostcalc.domain.usecase.di.useCaseModule
import com.erdees.foodcostcalc.ui.di.appModule
import com.erdees.foodcostcalc.utils.di.utilModule
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.initialize
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.context.unloadKoinModules
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

        Firebase.initialize(this)
        Firebase.appCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )
        Timber.i("Firebase App Check with Play Integrity Installed")

        startKoin()
        Timber.i("Koin Started!")
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