package com.erdees.foodcostcalc.ui.screens.paywall

import com.erdees.foodcostcalc.domain.model.premiumSubscription.Plan
import com.erdees.foodcostcalc.domain.model.premiumSubscription.PremiumSubscription

/**
 * Represents the state of the paywall screen.
 */
data class PaywallUiState(
    val premiumSubscription: PremiumSubscription? = null,
    val userAlreadySubscribes: Boolean = false,
    val screenLaunchedWithoutSubscription: Boolean,
    val selectedPlan: Plan? = null,
    val isLoading: Boolean = false,
    val error: Error? = null,
    val restoreState: RestoreState = RestoreState.Idle,
)