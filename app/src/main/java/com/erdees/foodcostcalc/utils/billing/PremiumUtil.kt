package com.erdees.foodcostcalc.utils.billing

import android.app.Activity
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.erdees.foodcostcalc.data.Preferences
import com.google.common.collect.ImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class PremiumUtil(private val preferences: Preferences) {

    private val _productDetails = MutableStateFlow<List<ProductDetails>>(emptyList())
    val productDetails: StateFlow<List<ProductDetails>> = _productDetails

    val purchaseUpdateListener = PurchasesUpdatedListener { result, purchases ->
        if (result.responseCode == BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        }
    }

    var billingClient: BillingClient? = null

    private fun handlePurchase(purchase: Purchase) {
        billingClient?.let { bc ->

            CoroutineScope(Dispatchers.Main).launch {
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    if (!purchase.isAcknowledged) {
                        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(purchase.purchaseToken)
                            .build()
                        withContext(Dispatchers.IO) {
                            // MAKE SURE SURE TO SAVE THIS IN PREFERENCES
                            preferences.setUserHasActiveSubscription(true)
                            // ACKNOWLEDGE PURCHASE
                            bc.acknowledgePurchase(acknowledgePurchaseParams)
                        }
                    }
                }
            }
        }
    }


    fun billingSetup(dispatcher: CoroutineDispatcher = Dispatchers.IO) {
        billingClient?.let { bc ->
            Timber.i("billingSetup()")
            bc.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    Timber.i("onBillingSetupFinished() , responseCode: ${result.responseCode}")
                    if (result.responseCode == BillingResponseCode.OK) {
                        val params = QueryPurchasesParams.newBuilder()
                            .setProductType(BillingClient.ProductType.SUBS)

                        // Check whether the user already subscribes
                        bc.queryPurchasesAsync(params.build()) { billingResult, purchase ->

                            if (billingResult.responseCode == BillingResponseCode.OK) {
                                Timber.i(
                                    "queryPurchasesAsync() , responseCode: ${billingResult.responseCode}, purchase: $purchase"
                                )
                                if (purchase.isNotEmpty()) {
                                    Timber.i("User already has subscription")
                                    CoroutineScope(dispatcher).launch {
                                        preferences.setUserHasActiveSubscription(true)
                                    }
                                } else {
                                    Timber.i("User does not have subscription")
                                    CoroutineScope(dispatcher).launch {
                                        preferences.setUserHasActiveSubscription(false)
                                    }
                                }
                            }
                        }


                        val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
                            .setProductList(
                                ImmutableList.of(
                                    QueryProductDetailsParams.Product.newBuilder()
                                        .setProductId(PRODUCT_ID)
                                        .setProductType(BillingClient.ProductType.SUBS)
                                        .build()
                                )
                            ).build()

                        bc.queryProductDetailsAsync(
                            queryProductDetailsParams
                        ) { billingResult, productDetailsList ->
                            Timber.i(
                                "queryProductDetailsAsync() , responseCode: ${billingResult.responseCode}, productDetailsList: $productDetailsList"
                            )
                            when (billingResult.responseCode) {
                                BillingResponseCode.OK -> {
                                    _productDetails.value = productDetailsList
                                }
                            }
                        }
                    }
                }

                override fun onBillingServiceDisconnected() {
                    // Handle billing service disconnection
                }
            })
        }
    }

    fun initializePurchase(
        productDetails: ProductDetails,
        selectedOfferToken: String,
        billingClient: BillingClient,
        activity: Activity
    ) {
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(selectedOfferToken)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    /**
     * Manually triggers a check for existing purchases and provides the result via a callback.
     * This is intended to be called by a "Restore Purchases" button.
     *
     * @param onRestoreFinished A callback that returns the result of the query.
     */
    fun restorePurchases(onRestoreFinished: (result: BillingResult, hasRestored: Boolean) -> Unit) {
        val client = billingClient ?: run {
            // If the client is null, we can't proceed. Report an error.
            onRestoreFinished(
                BillingResult.newBuilder().setResponseCode(BillingResponseCode.BILLING_UNAVAILABLE).build(),
                false
            )
            return
        }

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        client.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingResponseCode.OK && purchases.isNotEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    preferences.setUserHasActiveSubscription(true)
                }
                Timber.i("Restore successful. User has active subscription.")
                onRestoreFinished(billingResult, true)
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    preferences.setUserHasActiveSubscription(false)
                }
                Timber.i("Restore finished. No active subscriptions found.")
                onRestoreFinished(billingResult, false)
            }
        }
    }

    companion object {
        const val PRODUCT_ID = "food.cost.calculator.premium.account"
        const val SUBSCRIPTION_MONTHLY_PLAN_ID = "premium-mode-monthly-plan"
        const val SUBSCRIPTION_YEARLY_PLAN_ID = "premium-mode-yearly-plan"
    }
}