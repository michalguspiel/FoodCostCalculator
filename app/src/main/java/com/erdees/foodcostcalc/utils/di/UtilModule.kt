package com.erdees.foodcostcalc.utils.di

import com.erdees.foodcostcalc.utils.MyDispatchers
import com.erdees.foodcostcalc.utils.MyDispatchersImpl
import com.erdees.foodcostcalc.utils.billing.PremiumUtil
import org.koin.dsl.module

val utilModule = module {
    single<PremiumUtil> { PremiumUtil(get()) }
    single<MyDispatchers> { MyDispatchersImpl() }
}
