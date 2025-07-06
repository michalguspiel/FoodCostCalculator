package com.erdees.foodcostcalc.ui.di

import com.erdees.foodcostcalc.ui.spotlight.Spotlight
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module

val appModule = module {
    single { Spotlight(CoroutineScope(SupervisorJob() + Dispatchers.Main)) }
}