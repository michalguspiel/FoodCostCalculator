package com.erdees.foodcostcalc.data.di

import com.erdees.foodcostcalc.data.repository.AnalyticsRepository
import com.erdees.foodcostcalc.data.repository.AnalyticsRepositoryImpl
import com.erdees.foodcostcalc.data.repository.DishRepository
import com.erdees.foodcostcalc.data.repository.DishRepositoryImpl
import com.erdees.foodcostcalc.data.repository.HalfProductRepository
import com.erdees.foodcostcalc.data.repository.HalfProductRepositoryImpl
import com.erdees.foodcostcalc.data.repository.ProductRepository
import com.erdees.foodcostcalc.data.repository.ProductRepositoryImpl
import com.erdees.foodcostcalc.data.repository.RecipeRepository
import com.erdees.foodcostcalc.data.repository.RecipeRepositoryImpl
import com.google.firebase.analytics.FirebaseAnalytics
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val repositoryModule = module {
    single<DishRepository> { DishRepositoryImpl() }
    single<ProductRepository> { ProductRepositoryImpl() }
    single<HalfProductRepository> { HalfProductRepositoryImpl() }
    single<RecipeRepository> { RecipeRepositoryImpl() }

    single<FirebaseAnalytics> { FirebaseAnalytics.getInstance(androidApplication()) }
    single<AnalyticsRepository> { AnalyticsRepositoryImpl(get()) }
}
