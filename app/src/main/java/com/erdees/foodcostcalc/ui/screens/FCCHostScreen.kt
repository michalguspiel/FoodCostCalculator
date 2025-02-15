package com.erdees.foodcostcalc.ui.screens

import android.os.Bundle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
fun FCCHostScreen(analyticsRepository: AnalyticsRepository = koinInject()) {
    val navController = rememberNavController()

    navController.addOnDestinationChangedListener { _, destination, _ ->
        val bundle = Bundle()
        val screen =
            destination.route?.removePrefix("com.erdees.foodcostcalc.ui.navigation.FCCScreen.")
        bundle.putString(Constants.Analytics.SCREEN_NAME, screen ?: "")
        analyticsRepository.logEvent(Constants.Analytics.NAV_EVENT, bundle)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination?.route
            NavigationBar {
                FCCScreen.bottomNavigationScreens.forEach { item ->
                    NavigationBarItem(
                        selected = currentDestination == item::class.qualifiedName,
                        onClick = {
                            if (currentDestination != item::class.qualifiedName) {
                                navController.navigate(item)
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
    ) { paddingValues ->
        FCCNavigation(paddingValues = paddingValues, navController = navController)
    }
}
