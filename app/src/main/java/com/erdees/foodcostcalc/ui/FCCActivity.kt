package com.erdees.foodcostcalc.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.erdees.foodcostcalc.ui.screens.hostScreen.FCCHostScreen
import com.erdees.foodcostcalc.ui.theme.FCCTheme
import timber.log.Timber

class FCCActivity : AppCompatActivity() {

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
    }
}