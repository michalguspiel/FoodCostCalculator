package com.erdees.foodcostcalc.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.erdees.foodcostcalc.data.repository.AnalyticsRepository
import com.erdees.foodcostcalc.data.repository.DishRepository
import com.erdees.foodcostcalc.ui.screens.hostScreen.FCCHostScreen
import com.erdees.foodcostcalc.ui.theme.FCCTheme
import com.erdees.foodcostcalc.utils.Constants
import com.erdees.foodcostcalc.utils.MyDispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import timber.log.Timber

class FCCActivity : AppCompatActivity() {

    private val dishRepository: DishRepository by inject()
    private val analyticsRepository: AnalyticsRepository by inject()
    private val dispatches: MyDispatchers by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        Timber.i("onCreate")

        setContent {
            FCCTheme {
                FCCHostScreen()
            }
        }

        lifecycleScope.launch(dispatches.ioDispatcher) {
            updateDishCountUserProperty()
        }
    }

    private suspend fun updateDishCountUserProperty() {
        try {
            val dishCount = dishRepository.getDishCount()
            analyticsRepository.setUserProperty(
                Constants.Analytics.UserProperties.DISH_COUNT,
                dishCount.toString()
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to update dish count user property")
        }
    }
}