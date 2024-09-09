package com.erdees.foodcostcalc.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.erdees.foodcostcalc.ui.screens.FCCHostScreen
import com.erdees.foodcostcalc.ui.theme.FCCTheme
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FCCActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        CoroutineScope(Dispatchers.IO).launch {
            val testDevices = listOf(
                "3C07BBF025D37C2860EE53088321FCB2",
                "6D82FB226E12482C4555652147F98C12"
            )
            val adsRequestConfiguration = RequestConfiguration.Builder()
                .setTestDeviceIds(testDevices)
                .build()
            MobileAds.setRequestConfiguration(adsRequestConfiguration)
            MobileAds.initialize(this@FCCActivity) {
                // Initialization complete. It is now safe to show ads.
            }
        }
        setContent {
            FCCTheme {
                FCCHostScreen()
            }
        }
    }
}