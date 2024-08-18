package com.erdees.foodcostcalc.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.erdees.foodcostcalc.databinding.ActivityMainBinding
import com.erdees.foodcostcalc.ui.screens.FCCHostScreen
import com.erdees.foodcostcalc.ui.theme.FCCTheme

class FCCActivity : AppCompatActivity() {

  lateinit var binding: ActivityMainBinding
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      FCCTheme {
        FCCHostScreen()
      }
    }
  }
}