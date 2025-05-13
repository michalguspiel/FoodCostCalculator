package com.erdees.foodcostcalc.ui.screens.hostScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.Preferences
import com.erdees.foodcostcalc.ui.navigation.FCCScreen
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class FCCHostScreenViewModel : ViewModel(), KoinComponent {
    private val preferences: Preferences by inject()

    private val showHalfProducts = preferences.showHalfProducts.stateIn(
        viewModelScope, SharingStarted.Eagerly, null
    )

    val bottomNavigationScreens = showHalfProducts.map {
        FCCScreen.bottomNavigationScreens.let { screens ->
            when (it) {
                null -> null
                false -> {
                    screens.filter { screen -> screen != FCCScreen.HalfProducts }
                }

                else -> {
                    screens
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)
}