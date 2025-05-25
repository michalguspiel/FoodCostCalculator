package com.erdees.foodcostcalc.data.di

import com.erdees.foodcostcalc.data.AppRoomDataBase
import com.erdees.foodcostcalc.data.Preferences
import com.erdees.foodcostcalc.data.PreferencesImpl
import com.erdees.foodcostcalc.data.db.dao.dish.DishDao
import com.erdees.foodcostcalc.data.db.dao.dish.HalfProductDishDao
import com.erdees.foodcostcalc.data.db.dao.dish.ProductDishDao
import com.erdees.foodcostcalc.data.db.dao.featurerequest.FeatureRequestDao
import com.erdees.foodcostcalc.data.db.dao.halfproduct.HalfProductDao
import com.erdees.foodcostcalc.data.db.dao.halfproduct.ProductHalfProductDao
import com.erdees.foodcostcalc.data.db.dao.product.ProductDao
import com.erdees.foodcostcalc.data.db.dao.recipe.RecipeDao
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val dbModule = module {
    single<Preferences> { PreferencesImpl(androidApplication()) }
    single<AppRoomDataBase> { AppRoomDataBase.getDatabase(androidApplication()) }

    single<ProductDao> { get<AppRoomDataBase>().productDao() }
    single<HalfProductDao> { get<AppRoomDataBase>().halfProductDao() }
    single<DishDao> { get<AppRoomDataBase>().dishDao() }
    single<ProductDishDao> { get<AppRoomDataBase>().productDishDao() }
    single<HalfProductDishDao> { get<AppRoomDataBase>().halfProductDishDao() }
    single<ProductHalfProductDao> { get<AppRoomDataBase>().productHalfProductDao() }
    single<RecipeDao> { get<AppRoomDataBase>().recipeDao() }
    single<FeatureRequestDao> { get<AppRoomDataBase>().featureRequestDao() }
}
