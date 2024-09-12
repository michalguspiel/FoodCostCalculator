package com.erdees.foodcostcalc.ui.screens

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
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.erdees.foodcostcalc.ui.navigation.FCCNavigation
import com.erdees.foodcostcalc.ui.navigation.FCCScreen

@Composable
fun FCCHostScreen(modifier: Modifier = Modifier) {
  val navController = rememberNavController()
  Scaffold(
    modifier = Modifier.fillMaxSize(),
    bottomBar = {
      val navBackStackEntry by navController.currentBackStackEntryAsState()
      val currentDestination = navBackStackEntry?.destination?.route
      NavigationBar {
        FCCScreen.bottomNavigationScreens.forEach { item ->
          NavigationBarItem(
            selected =
            currentDestination == item::class.qualifiedName,
            onClick = {
              if (currentDestination != item::class.qualifiedName) {
                navController.navigate(item)
              }
            },
            icon = {
              item.iconResourceId?.let {
                Icon(
                  modifier = Modifier.size(24.dp),
                  painter = painterResource(id = item.iconResourceId),
                  contentDescription = "" // todo content description
                )
              }
            },
            label = { Text(text = item.name, style = MaterialTheme.typography.labelMedium) })
        }
      }
    }
  ) { paddingValues ->
    FCCNavigation(paddingValues = paddingValues, navController = navController)
  }
}
