package com.erdees.foodcostcalc.ui.screens.subscriptionScreen

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.ProductDetails
import com.erdees.foodcostcalc.data.Preferences
import com.erdees.foodcostcalc.domain.model.premiumSubscription.Plan
import com.erdees.foodcostcalc.domain.model.premiumSubscription.PremiumSubscription
import com.erdees.foodcostcalc.ext.toPremiumSubscription
import com.erdees.foodcostcalc.utils.billing.PremiumUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

data object MissingBillingClient : Error("BillingClient is missing") {
    private fun readResolve(): Any = MissingBillingClient
}

data object PlanNotSelected : Error("Plan not selected") {
    private fun readResolve(): Any = PlanNotSelected
}

data object MissingProductDetails : Error("Plan not selected") {
    private fun readResolve(): Any = MissingProductDetails
}

/**
 * Represents the state of the subscription screen.
 *
 *
 * @param productDetails Data model of the premium subscription. Necessary to make the purchase
 * @param premiumSubscription Domain level premium subscription.
 * @param userAlreadySubscribes Whether the user already subscribes to the premium subscription.
 * @param screenLaunchedWithoutSubscription Whether the screen was launched without a subscription. Necessary to determine if confetti should be launched or not.
 * @param selectedPlan The selected plan.
 * @param isLoading Whether the screen is loading.
 * @param error The error.
 * */
data class SubscriptionScreenState(
    val productDetails: ProductDetails? = null,
    val premiumSubscription: PremiumSubscription? = null,
    val userAlreadySubscribes: Boolean = false,
    val screenLaunchedWithoutSubscription: Boolean,
    val selectedPlan: Plan? = null,
    val isLoading: Boolean = false,
    val error: Error? = null
)

class SubscriptionViewModel : ViewModel(), KoinComponent {

    private val preferences: Preferences by inject()
    private val premiumUtil: PremiumUtil by inject()

    private val _screenState = MutableStateFlow<SubscriptionScreenState?>(null)
    val screenState: StateFlow<SubscriptionScreenState?> = _screenState

    init {
        viewModelScope.launch {
            val userHasSub = preferences.userHasActiveSubscription().first()
            val productDetails = premiumUtil.productDetails.value.firstOrNull()
            val subscription = productDetails?.toPremiumSubscription()
            _screenState.value = SubscriptionScreenState(
                productDetails = productDetails,
                premiumSubscription = subscription,
                selectedPlan = subscription?.monthlyPlan,
                userAlreadySubscribes = userHasSub,
                screenLaunchedWithoutSubscription = !userHasSub
            )
        }
    }


    fun onPlanSelected(plan: Plan) {
        _screenState.value = _screenState.value?.copy(selectedPlan = plan)
    }

    /**
     * This function checks the subscription status of the user.
     *
     * It is called onResume, so that when user made the purchase the screen will be updated.
     * */
    fun updateSubscriptionStatus() {
        viewModelScope.launch {
            _screenState.value = _screenState.value?.copy(userAlreadySubscribes = preferences.userHasActiveSubscription().first())

        }
    }

    fun onSubscribeClicked(activity: Activity?) {
        val billingClient = premiumUtil.billingClient
        if (activity == null || billingClient == null) {
            setError(MissingBillingClient)
            return
        }

        val selectedPlan = _screenState.value?.selectedPlan

        if (selectedPlan == null) {
            setError(PlanNotSelected)
            return
        }

        val productDetails = _screenState.value?.productDetails
        if (productDetails == null) {
            setError(MissingProductDetails)
            return
        }

        premiumUtil.initializePurchase(
            productDetails = productDetails,
            activity = activity,
            selectedOfferToken = selectedPlan.offerIdToken,
            billingClient = billingClient
        )
    }

    private fun setError(error: Error) {
        Timber.i("setError: $error")
        _screenState.value = _screenState.value?.copy(error = error, isLoading = false)
    }

    fun acknowledgeError() {
        _screenState.value = _screenState.value?.copy(error = null)
    }

    fun onManageSubscription(context: Context) {
        val link =
            "https://play.google.com/store/account/subscriptions?sku=${PremiumUtil.PRODUCT_ID}&package=com.erdees.foodcostcalc"
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = link.toUri()
            setPackage("com.android.vending")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        context.startActivity(intent)
    }
}