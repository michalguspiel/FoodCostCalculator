package com.erdees.foodcostcalc.ui.screens.hostScreen

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDestination
import com.erdees.foodcostcalc.data.Preferences
import com.erdees.foodcostcalc.data.repository.AnalyticsRepository
import com.erdees.foodcostcalc.ui.navigation.FCCScreen
import com.erdees.foodcostcalc.utils.Constants
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class FCCHostScreenViewModel : ViewModel(), KoinComponent {
    private val preferences: Preferences by inject()
    private val analyticsRepository: AnalyticsRepository by inject()

    private val showHalfProducts = preferences.showHalfProducts.stateIn(
        viewModelScope, SharingStarted.Eagerly, null
    )

    private val bottomNavScreens =
        listOf(FCCScreen.Products, FCCScreen.HalfProducts, FCCScreen.Dishes, FCCScreen.Settings)
    val filteredBottomNavScreens: StateFlow<List<FCCScreen>?> =
        showHalfProducts.filterNotNull().map { show ->
            if (show) bottomNavScreens
            else bottomNavScreens.filterNot { it == FCCScreen.HalfProducts }
        }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    fun showNavBar(
        currentDestination: String?,
    ): Boolean {
        return when (currentDestination) {
            FCCScreen.Products::class.qualifiedName,
            FCCScreen.HalfProducts::class.qualifiedName,
            FCCScreen.Dishes::class.qualifiedName,
            FCCScreen.Settings::class.qualifiedName,
                -> true

            else -> false
        }
    }

    private var lastLoggedRoute: String? = null

    fun logNavigation(destination: NavDestination){
        val route = destination.route
        if (route == null || route == lastLoggedRoute) return
        lastLoggedRoute = route
        val screen =
            route.removePrefix("com.erdees.foodcostcalc.ui.navigation.FCCScreen.")
        val bundle = Bundle().apply {
            putString(Constants.Analytics.SCREEN_NAME, screen)
        }
        analyticsRepository.logEvent(Constants.Analytics.NAV_EVENT, bundle)
    }
}