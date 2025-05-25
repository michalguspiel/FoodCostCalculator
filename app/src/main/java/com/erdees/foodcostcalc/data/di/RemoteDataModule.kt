package com.erdees.foodcostcalc.data.di

import com.erdees.foodcostcalc.data.remote.FeatureRequestService
import org.koin.dsl.module

val remoteDataModule = module {
    single<FeatureRequestService> { FeatureRequestService() }
}