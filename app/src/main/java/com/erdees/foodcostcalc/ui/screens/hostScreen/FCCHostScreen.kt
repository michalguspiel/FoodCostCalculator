package com.erdees.foodcostcalc.ui.screens.hostScreen

import android.os.Bundle
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.erdees.foodcostcalc.data.repository.AnalyticsRepository
import com.erdees.foodcostcalc.ui.navigation.FCCNavigation
import com.erdees.foodcostcalc.ui.navigation.FCCScreen
import com.erdees.foodcostcalc.ui.navigation.Screen
import com.erdees.foodcostcalc.utils.Constants
import org.koin.compose.koinInject

@Screen
@Composable
fun FCCHostScreen(
    analyticsRepository: AnalyticsRepository = koinInject(),
    viewModel: FCCHostScreenViewModel = viewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination by remember(navBackStackEntry) { mutableStateOf(navBackStackEntry?.destination?.route) }
    var isNavigationBarVisible by rememberSaveable { mutableStateOf(showNavBar(currentDestination, analyticsRepository)) }
    val bottomNavigationScreens by viewModel.bottomNavigationScreens.collectAsState()

    LaunchedEffect(Unit) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            isNavigationBarVisible = showNavBar(destination.route, analyticsRepository)
            val screen =
                destination.route?.removePrefix("com.erdees.foodcostcalc.ui.navigation.FCCScreen.")
            val bundle = Bundle().apply {
                putString(Constants.Analytics.SCREEN_NAME, screen ?: "")
            }
            analyticsRepository.logEvent(Constants.Analytics.NAV_EVENT, bundle)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            AnimatedVisibility(isNavigationBarVisible && bottomNavigationScreens != null) {
                NavigationBar {
                    bottomNavigationScreens?.forEach { item ->
                        NavigationBarItem(
                            selected = currentDestination == item::class.qualifiedName,
                            onClick = {
                                if (currentDestination != item::class.qualifiedName) {
                                    navController.navigate(item) {
                                        popUpTo(item) { inclusive = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    modifier = Modifier.size(24.dp),
                                    painter = painterResource(id = item.iconResourceId),
                                    contentDescription = stringResource(id = item.iconResourceId)
                                )
                            },
                            label = {
                                Text(
                                    text = stringResource(id = item.nameStringRes),
                                    style = MaterialTheme.typography.labelMedium,
                                    textAlign = TextAlign.Center
                                )
                            })
                    }
                }
            }
        }
    ) { paddingValues ->
        FCCNavigation(paddingValues = paddingValues, navController = navController)
    }
}

private fun showNavBar(
    currentDestination: String?,
    analyticsRepository: AnalyticsRepository
): Boolean {
    return FCCScreen.bottomNavigationScreens.mapNotNull {
        try {
            it::class.qualifiedName
        } catch (e: Exception) {
            analyticsRepository.logException(e, Bundle().apply {
                putString(Constants.Analytics.Exceptions.SHOW_NAV_BAR, currentDestination)
            })
            null
        }
    }.contains(currentDestination)
}