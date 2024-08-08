package com.erdees.foodcostcalc.data.di

import com.erdees.foodcostcalc.data.repository.DishRepository
import com.erdees.foodcostcalc.data.repository.DishRepositoryImpl
import com.erdees.foodcostcalc.data.repository.HalfProductRepository
import com.erdees.foodcostcalc.data.repository.HalfProductRepositoryImpl
import com.erdees.foodcostcalc.data.repository.ProductRepository
import com.erdees.foodcostcalc.data.repository.ProductRepositoryImpl
import org.koin.dsl.module

val repositoryModule = module {
  single<DishRepository> { DishRepositoryImpl() }
  single<ProductRepository> { ProductRepositoryImpl() }
  single<HalfProductRepository> { HalfProductRepositoryImpl() }
}
