package com.erdees.foodcostcalc.domain.usecase.di

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
}