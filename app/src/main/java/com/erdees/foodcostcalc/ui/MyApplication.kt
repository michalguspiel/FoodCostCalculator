package com.erdees.foodcostcalc.ui

import android.app.Application
import com.erdees.foodcostcalc.data.di.dbModule
import com.erdees.foodcostcalc.data.di.repositoryModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            // declare used Android context
            androidContext(this@MyApplication)
            // declare modules
            modules(
                dbModule, repositoryModule
            )
        }
    }
}
