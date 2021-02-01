package com.example.foodcostcalc

import com.example.foodcostcalc.fragments.AddViewModelFactory
import com.example.foodcostcalc.data.DataBase
import com.example.foodcostcalc.data.Repository


// Finally a singleton which doesn't need anything passed to the constructor
object InjectorUtils {

    // This will be called from QuotesActivity
    fun provideAddViewModelFactory(): AddViewModelFactory {
        // ViewModelFactory needs a repository, which in turn needs a DAO from a database
        // The whole dependency tree is constructed right here, in one place
        val repository = Repository.getInstance(DataBase.getInstance().productDao)
        return AddViewModelFactory(repository)
    }
}