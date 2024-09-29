package com.erdees.foodcostcalc.ui

import android.app.Application
import android.util.Log
import com.erdees.foodcostcalc.data.di.dbModule
import com.erdees.foodcostcalc.data.di.repositoryModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.i("MyApplication", "onCreate")
        startKoin()
    }

    private fun startKoin(){
        Log.i(TAG, "startKoin()")
        startKoin {
            // declare used Android context
            androidContext(this@MyApplication)
            // declare modules
            modules(
                dbModule, repositoryModule
            )
        }
    }

    /**
     * Necessary action after recreating database from online backup.
     * */
    fun restartKoin(){
        Log.i(TAG, "restartKoin()")
        stopKoin()
        startKoin()
    }

    companion object {
        private const val TAG = "MyApplication"
    }
}
