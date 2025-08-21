package com.erdees.foodcostcalc.ui.screens.loyaltyReward

import androidx.lifecycle.ViewModel
import com.erdees.foodcostcalc.data.Preferences
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class LoyaltyRewardViewModel : ViewModel(), KoinComponent {
    private val preferences: Preferences by inject()

    suspend fun markLoyaltyScreenSeen() {
        preferences.setHasSeenLoyaltyScreen(true)
    }
}