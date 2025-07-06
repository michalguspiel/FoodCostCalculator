package com.erdees.foodcostcalc.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.erdees.foodcostcalc.BuildConfig
import com.erdees.foodcostcalc.data.Preferences
import com.erdees.foodcostcalc.domain.model.onboarding.OnboardingState
import com.erdees.foodcostcalc.ui.screens.hostScreen.FCCHostScreen
import com.erdees.foodcostcalc.ui.theme.FCCTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject
import timber.log.Timber

class FCCActivity : AppCompatActivity() {

    private val preferences: Preferences by inject(Preferences::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        Timber.i("onCreate")

        if (BuildConfig.DEBUG) {
            // Reset onboarding state in debug builds
            CoroutineScope(Dispatchers.IO).launch {
                preferences.setOnboardingState(OnboardingState.NOT_STARTED)
            }
        }

        setContent {
            FCCTheme {
                FCCHostScreen()
            }
        }
    }
}