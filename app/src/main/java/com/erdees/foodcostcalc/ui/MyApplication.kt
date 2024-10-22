package com.erdees.foodcostcalc.ui

import android.app.Application
import android.util.Log
import com.erdees.foodcostcalc.data.di.dbModule
import com.erdees.foodcostcalc.data.di.repositoryModule
import com.erdees.foodcostcalc.utils.di.utilModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.context.unloadKoinModules

class MyApplication : Application() {

    // modules that need to be restarted after database recreation
    private val reloadableModules = listOf(dbModule, repositoryModule)

    override fun onCreate() {
        super.onCreate()
        Log.i("MyApplication", "onCreate")
        startKoin()
    }

    private fun startKoin() {
        Log.i(TAG, "startKoin()")
        startKoin {
            // declare used Android context
            androidContext(this@MyApplication)
            // declare modules
            modules(
                reloadableModules + utilModule
            )
        }
    }

    /**
     * Necessary action after recreating database from online backup.
     * */
    fun restartDataModule() {
        Log.i(TAG, "restartDataModule()")
        unloadKoinModules(reloadableModules)
        loadKoinModules(reloadableModules)
    }

    companion object {
        private const val TAG = "MyApplication"
    }
}
