package com.erdees.foodcostcalc.ui.screens.paywall

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
import com.erdees.foodcostcalc.data.Preferences
import com.erdees.foodcostcalc.domain.model.premiumSubscription.Plan
import com.erdees.foodcostcalc.ext.toPremiumSubscription
import com.erdees.foodcostcalc.utils.billing.PremiumUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

data object PaywallMissingBillingClient : Error("BillingClient is missing") {
    private fun readResolve(): Any = PaywallMissingBillingClient
}

data object PaywallPlanNotSelected : Error("Plan not selected") {
    private fun readResolve(): Any = PaywallPlanNotSelected
}

data object PaywallMissingProductDetails : Error("Product details missing") {
    private fun readResolve(): Any = PaywallMissingProductDetails
}

sealed class RestoreState {
    object Idle : RestoreState()
    object Success : RestoreState()
    object NoPurchases : RestoreState()
    data class Error(val message: String) : RestoreState()
}



/**
 * Represents the state of the paywall screen.
 *
 * @param availablePlans List of available subscription plans from Google Play Billing
 * @param selectedPlan The currently selected plan (monthly or annual)
 * @param isLoading Whether the screen is loading
 * @param error Any error that occurred
 */
data class PaywallUiState(
    val availablePlans: List<Plan> = emptyList(),
    val selectedPlan: Plan? = null,
    val isLoading: Boolean = false,
    val error: Error? = null,
    val restoreState: RestoreState = RestoreState.Idle,
) {
    val monthlyPlan: Plan? = availablePlans.find { it.billingPeriod == "P1M" }
    val yearlyPlan: Plan? = availablePlans.find { it.billingPeriod == "P1Y" }
}

class PaywallViewModel : ViewModel(), KoinComponent {

    private val preferences: Preferences by inject()
    private val premiumUtil: PremiumUtil by inject()

    private val _uiState = MutableStateFlow(PaywallUiState(isLoading = true))
    val uiState: StateFlow<PaywallUiState> = _uiState

    private var cachedProductDetails: ProductDetails? = null

    init {
        loadSubscriptionPlans()
    }

    private fun loadSubscriptionPlans() {
        viewModelScope.launch {
            try {
                val productDetails = premiumUtil.productDetails.value.firstOrNull()
                if (productDetails != null) {
                    cachedProductDetails = productDetails
                    val subscription = productDetails.toPremiumSubscription()
                    val plans = listOf(subscription.monthlyPlan, subscription.yearlyPlan)
                    
                    _uiState.value = PaywallUiState(
                        availablePlans = plans,
                        selectedPlan = subscription.yearlyPlan,
                        isLoading = false
                    )
                } else {
                    _uiState.value = PaywallUiState(
                        isLoading = false,
                        error = PaywallMissingProductDetails
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load subscription plans")
                _uiState.value = PaywallUiState(
                    isLoading = false,
                    error = PaywallMissingProductDetails
                )
            }
        }
    }

    fun selectPlan(plan: Plan) {
        _uiState.value = _uiState.value.copy(selectedPlan = plan)
    }

    fun onUpgradeClicked(activity: Activity?) {
        val billingClient = premiumUtil.billingClient
        if (activity == null || billingClient == null) {
            setError(PaywallMissingBillingClient)
            return
        }

        val selectedPlan = _uiState.value.selectedPlan
        if (selectedPlan == null) {
            setError(PaywallPlanNotSelected)
            return
        }

        val productDetails = cachedProductDetails
        if (productDetails == null) {
            setError(PaywallMissingProductDetails)
            return
        }

        premiumUtil.initializePurchase(
            productDetails = productDetails,
            activity = activity,
            selectedOfferToken = selectedPlan.offerIdToken,
            billingClient = billingClient
        )
    }

    fun onRestorePurchases() {
        _uiState.value = _uiState.value.copy(isLoading = true)

        premiumUtil.restorePurchases { result, hasRestored ->
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isLoading = false)

                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    if (hasRestored) {
                        _uiState.value = _uiState.value.copy(restoreState = RestoreState.Success)
                    } else {
                        _uiState.value = _uiState.value.copy(restoreState = RestoreState.NoPurchases)
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        restoreState = RestoreState.Error("Error: ${result.debugMessage}")
                    )
                }
            }
        }
    }

    fun resetRestoreState() {
        _uiState.value = _uiState.value.copy(restoreState = RestoreState.Idle)
    }

    private fun setError(error: Error) {
        Timber.e("PaywallViewModel error: $error")
        _uiState.value = _uiState.value.copy(error = error, isLoading = false)
    }

    fun acknowledgeError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}