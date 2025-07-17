package com.erdees.foodcostcalc.domain.usecase.di

import com.erdees.foodcostcalc.domain.usecase.CopyDishUseCase
import com.erdees.foodcostcalc.domain.usecase.SaveDishUseCase
import com.erdees.foodcostcalc.domain.usecase.SubmitFeatureRequestUseCase
import org.koin.dsl.module

val useCaseModule = module {
    // Provide the use case
    factory {
        SubmitFeatureRequestUseCase(
            featureRequestService = get(),
            featureRequestRepository = get(),
            dispatchers = get()
        )
    }
    factory {
        CopyDishUseCase(
            dishRepository = get(),
            productRepository = get(),
            halfProductRepository = get(),
            recipeRepository = get(),
            analyticsRepository = get(),
            myDispatchers = get()
        )
    }
    factory {
        SaveDishUseCase(
            dishRepository = get(),
            myDispatchers = get()
        )
    }
}