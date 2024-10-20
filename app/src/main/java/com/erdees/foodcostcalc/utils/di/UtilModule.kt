package com.erdees.foodcostcalc.utils.di

import com.erdees.foodcostcalc.utils.billing.PremiumUtil
import org.koin.dsl.module

val utilModule = module {
    single<PremiumUtil> { PremiumUtil(get()) }
}
