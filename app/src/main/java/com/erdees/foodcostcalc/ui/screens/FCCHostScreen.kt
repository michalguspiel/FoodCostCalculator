package com.erdees.foodcostcalc.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.erdees.foodcostcalc.ui.navigation.FCCNavigation
import com.erdees.foodcostcalc.ui.navigation.FCCScreen

@Composable
fun FCCHostScreen(modifier: Modifier = Modifier) {
  val navController = rememberNavController()
  // A surface container using the 'background' color from the theme
  Scaffold(
    modifier = Modifier.fillMaxSize(),
    bottomBar = {
      val navBackStackEntry by navController.currentBackStackEntryAsState()
      val currentDestination = navBackStackEntry?.destination?.route
      // TODO SIDE DRAWER
      NavigationBar {
        FCCScreen.bottomNavigationScreens.forEach { item ->
          NavigationBarItem(
            colors = NavigationBarItemColors(
              selectedIconColor = MaterialTheme.colorScheme.primary,
              unselectedIconColor = MaterialTheme.colorScheme.onSurface,
              selectedTextColor = MaterialTheme.colorScheme.primary,
              unselectedTextColor = MaterialTheme.colorScheme.onSurface,
              disabledIconColor = MaterialTheme.colorScheme.surface,
              disabledTextColor = MaterialTheme.colorScheme.onSurface,
              selectedIndicatorColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            ),
            selected =
              currentDestination == item::class.qualifiedName
            ,
            onClick = {
              if(currentDestination != item::class.qualifiedName) {
                navController.navigate(item)
              }
            },
            icon = {
              Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(id = item.iconResourceId),
                contentDescription = "" // todo content description
              )
            },
            label = { Text(text = item.name, style = MaterialTheme.typography.labelMedium) })
        }
      }
    }
  ) { paddingValues ->
    FCCNavigation(paddingValues = paddingValues, navController = navController)
  }
}