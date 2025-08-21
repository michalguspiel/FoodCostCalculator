package com.erdees.foodcostcalc.ui.screens.hostScreen

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDestination
import com.erdees.foodcostcalc.data.Preferences
import com.erdees.foodcostcalc.data.repository.AnalyticsRepository
import com.erdees.foodcostcalc.domain.manager.EntitlementManager
import com.erdees.foodcostcalc.domain.model.onboarding.OnboardingState
import com.erdees.foodcostcalc.ui.navigation.FCCScreen
import com.erdees.foodcostcalc.ui.spotlight.Spotlight
import com.erdees.foodcostcalc.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class FCCHostScreenViewModel : ViewModel(), KoinComponent {
    private val preferences: Preferences by inject()
    private val analyticsRepository: AnalyticsRepository by inject()
    private val entitlementManager: EntitlementManager by inject()
    val spotlight: Spotlight by inject()

    private val showHalfProducts = preferences.showHalfProducts.stateIn(
        viewModelScope, SharingStarted.Eagerly, null
    )

    private val onboardingState: StateFlow<OnboardingState?> =
        preferences.onboardingState
            .stateIn(viewModelScope, SharingStarted.Lazily, null)

    private val _startingDestination: MutableStateFlow<FCCScreen?> = MutableStateFlow(null)
    val startingDestination: StateFlow<FCCScreen?> = _startingDestination.onStart {
        getStartingDestination()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, FCCScreen.Dishes)

    private fun getStartingDestination() {
        viewModelScope.launch {
            val onboardingStateValue = onboardingState.filterNotNull().first()
            
            // First check if onboarding is needed
            if (onboardingStateValue == OnboardingState.NOT_STARTED) {
                _startingDestination.update { FCCScreen.Onboarding }
                return@launch
            }
            
            // Then check if loyalty screen should be shown
            val isLegacySubscriber = entitlementManager.isLegacySubscriber()
            val hasSeenLoyaltyScreen = preferences.hasSeenLoyaltyScreen.first()
            
            if (isLegacySubscriber && !hasSeenLoyaltyScreen) {
                _startingDestination.update { FCCScreen.LoyaltyReward }
                return@launch
            }
            
            // Default to Products screen
            _startingDestination.update { FCCScreen.Products }
        }
    }

    private val bottomNavScreens =
        listOf(FCCScreen.Products, FCCScreen.HalfProducts, FCCScreen.Dishes, FCCScreen.Settings)
    val filteredBottomNavScreens: StateFlow<List<FCCScreen>?> =
        showHalfProducts.filterNotNull().map { show ->
            if (show) bottomNavScreens
            else bottomNavScreens.filterNot { it == FCCScreen.HalfProducts }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun showNavBar(
        currentDestination: String?,
        isCompactHeight: Boolean = false
    ): Boolean {
        Timber.i("showNavBar called with currentDestination: $currentDestination")
        if (currentDestination == null) return false

        val shouldShowForDestination = bottomNavScreens.any { screen ->
            currentDestination == screen::class.qualifiedName
        }

        if (spotlight.isActive && spotlight.currentTarget?.canHideNavBar == true && isCompactHeight) {
            return false
        }

        return shouldShowForDestination
    }

    fun isCurrentDestinationSelected(
        currentDestination: String?,
        screen: FCCScreen
    ): Boolean {
        Timber.i("isCurrentDestinationSelected called with currentDestination: $currentDestination, screen: $screen")
        return if (currentDestination == null) return false
        else currentDestination == screen::class.qualifiedName
    }

    private var lastLoggedRoute: String? = null

    fun logNavigation(destination: NavDestination) {
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