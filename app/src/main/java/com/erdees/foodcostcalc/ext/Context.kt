package com.erdees.foodcostcalc.ext

import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.content.ContextWrapper
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

fun Context.getActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "settings",
    produceMigrations = { context ->
        listOf(SharedPreferencesMigration(context, "settings"))
    }
)

fun Context.vibrateForConfirmation() {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager =
            getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        getSystemService(VIBRATOR_SERVICE) as Vibrator
    }
    vibrator.vibrate(VibrationEffect.createOneShot(ShortVibration, VibrationEffect.DEFAULT_AMPLITUDE))
}

private const val ShortVibration = 200L