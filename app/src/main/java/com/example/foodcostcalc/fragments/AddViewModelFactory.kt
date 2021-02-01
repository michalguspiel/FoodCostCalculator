package com.example.foodcostcalc.fragments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.foodcostcalc.data.Repository

class AddViewModelFactory(private val repository: Repository):ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return AddViewModel(repository) as T
    }
}