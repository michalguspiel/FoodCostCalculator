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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
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


class PaywallViewModel : ViewModel(), KoinComponent {

    private val preferences: Preferences by inject()
    private val premiumUtil: PremiumUtil by inject()

    private val _uiState: MutableStateFlow<PaywallUiState?> = MutableStateFlow(null)
    val uiState: StateFlow<PaywallUiState?> = _uiState

    private var cachedProductDetails: ProductDetails? = null

    init {
        initializeState()
        observeSubscriptionChanges()
    }

    fun initializeState() {
        viewModelScope.launch {
            val currentActivePremiumPlan = preferences.currentActivePremiumPlan().first()
            val productDetails = premiumUtil.productDetails.value.firstOrNull()
            cachedProductDetails = productDetails
            val subscription = productDetails?.toPremiumSubscription()
            _uiState.value = PaywallUiState(
                premiumSubscription = subscription,
                selectedPlan = subscription?.monthlyPlan,
                currentActivePremiumPlanType = currentActivePremiumPlan,
                screenLaunchedWithoutSubscription = currentActivePremiumPlan == null,
            )
        }
        Timber.i("SubscriptionViewModel initialized with state: ${_uiState.value}")
    }

    private fun observeSubscriptionChanges() {
        viewModelScope.launch {
            preferences.currentActivePremiumPlan().collectLatest {
                Timber.i("Subscription change detected: $it")
                _uiState.value = _uiState.value?.copy(currentActivePremiumPlanType = it)
            }
        }
    }

    fun selectPlan(plan: Plan) {
        _uiState.value = _uiState.value?.copy(selectedPlan = plan)
    }

    fun onUpgradeClicked(activity: Activity?) {
        val billingClient = premiumUtil.billingClient
        if (activity == null || billingClient == null) {
            setError(PaywallMissingBillingClient)
            return
        }

        val selectedPlan = _uiState.value?.selectedPlan
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
        _uiState.value = _uiState.value?.copy(isLoading = true)

        premiumUtil.restorePurchases { result, restoredPlan ->
            viewModelScope.launch {
                _uiState.value = _uiState.value?.copy(isLoading = false)

                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    if (restoredPlan != null) {
                        _uiState.value = _uiState.value?.copy(restoreState = RestoreState.Success, currentActivePremiumPlanType = restoredPlan)
                    } else {
                        _uiState.value =
                            _uiState.value?.copy(restoreState = RestoreState.NoPurchases)
                    }
                } else {
                    _uiState.value = _uiState.value?.copy(
                        restoreState = RestoreState.Error("Error: ${result.debugMessage}")
                    )
                }
            }
        }
    }

    fun resetRestoreState() {
        _uiState.value = _uiState.value?.copy(restoreState = RestoreState.Idle)
    }

    private fun setError(error: Error) {
        Timber.e("PaywallViewModel error: $error")
        _uiState.value = _uiState.value?.copy(error = error, isLoading = false)
    }

    fun acknowledgeError() {
        _uiState.value = _uiState.value?.copy(error = null)
    }
}