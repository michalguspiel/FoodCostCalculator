package com.erdees.foodcostcalc.ui.screens.hostScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.erdees.foodcostcalc.ui.composables.ScreenLoadingOverlay
import com.erdees.foodcostcalc.ui.navigation.FCCNavigation
import com.erdees.foodcostcalc.ui.navigation.Screen
import com.erdees.foodcostcalc.ui.spotlight.SpotlightOverlay
import com.erdees.foodcostcalc.utils.Constants

@Composable
@Screen
fun FCCHostScreen(
    viewModel: FCCHostScreenViewModel = viewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination?.route
    val spotlight = viewModel.spotlight

    val isCompactHeight = with(LocalDensity.current) {
        LocalWindowInfo.current.containerSize.height.toDp() < Constants.UI.COMPACT_HEIGHT_THRESHOLD_DP.dp
    }

    val isNavigationBarVisible by rememberSaveable(currentDestination, spotlight.currentTarget, isCompactHeight) {
        mutableStateOf(viewModel.showNavBar(currentDestination, isCompactHeight))
    }
    val bottomNavigationScreens by viewModel.filteredBottomNavScreens.collectAsState()
    val startingDestination by viewModel.startingDestination.collectAsState()
    val safeStartDestination = startingDestination

    DisposableEffect(Unit) {
        onDispose {
            spotlight.clearTargetLambdas()
        }
    }

    LaunchedEffect(currentDestination) {
        navBackStackEntry?.destination?.let { viewModel.logNavigation(it) }
    }

    if (bottomNavigationScreens == null || safeStartDestination == null) {
        ScreenLoadingOverlay(Modifier.fillMaxSize())
    } else {
        SpotlightOverlay(spotlight = spotlight) {
            Scaffold(
                contentWindowInsets = WindowInsets.safeDrawing,
                modifier = Modifier
                    .fillMaxSize()
                    .run {
                        if (spotlight.isActive) {
                            this.clickable(enabled = false) {}
                        } else this
                    },
                bottomBar = {
                    AnimatedVisibility(isNavigationBarVisible) {
                        NavigationBar {
                            bottomNavigationScreens?.forEach { item ->
                                NavigationBarItem(
                                    selected = viewModel.isCurrentDestinationSelected(currentDestination, item),
                                    onClick = {
                                        if (!viewModel.isCurrentDestinationSelected(currentDestination, item)) {
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
                FCCNavigation(
                    paddingValues = paddingValues,
                    navController = navController,
                    modifier = Modifier.fillMaxSize(),
                    startDestination = safeStartDestination,
                )
            }
        }
    }
}