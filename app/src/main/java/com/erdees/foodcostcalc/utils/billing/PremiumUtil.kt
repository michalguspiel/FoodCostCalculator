package com.erdees.foodcostcalc.utils.billing

import android.app.Activity
import android.util.Log
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PremiumUtil(private val preferences: Preferences) {

    private val _productDetails = MutableStateFlow<List<ProductDetails>>(emptyList())
    val productDetails: StateFlow<List<ProductDetails>> = _productDetails

    val purchaseUpdateListener = PurchasesUpdatedListener { result, purchases ->
        if (result.responseCode == BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (result.responseCode == BillingResponseCode.USER_CANCELED) {
            // User canceled the purchase
        } else {
            // Handle other error cases
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
                        // MAKE SURE SURE TO SAVE THIS IN PREFERENCES
                        preferences.userHasActiveSubscription = true
                        // ACKNOWLEDGE PURCHASE
                        withContext(Dispatchers.IO) {
                            bc.acknowledgePurchase(acknowledgePurchaseParams)
                        }
                    }
                }
            }
        }
    }


    fun billingSetup() {
        billingClient?.let { bc ->
            Log.i(TAG, "billingSetup()")
            bc.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    Log.i(TAG, "onBillingSetupFinished() , responseCode: ${result.responseCode}")
                    if (result.responseCode == BillingResponseCode.OK) {
                        val params = QueryPurchasesParams.newBuilder()
                            .setProductType(BillingClient.ProductType.SUBS)

                        // Check whether the user already subscribes
                        bc.queryPurchasesAsync(params.build()) { billingResult, purchase ->

                            if (billingResult.responseCode == BillingResponseCode.OK) {
                                Log.i(
                                    "PremiumUtil",
                                    "queryPurchasesAsync() , responseCode: ${billingResult.responseCode}, purchase: $purchase"
                                )
                                if (purchase.isNotEmpty()) {
                                    Log.i(TAG, "User already has subscription")
                                    preferences.userHasActiveSubscription = true
                                } else {
                                    Log.i(TAG, "User does not have subscription")
                                    preferences.userHasActiveSubscription = false
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
                            Log.i(
                                TAG,
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

    companion object {
        private const val TAG = "PremiumUtil"
        const val PRODUCT_ID = "food.cost.calculator.premium.account"
        const val SUBSCRIPTION_MONTHLY_PLAN_ID = "premium-mode-monthly-plan"
        const val SUBSCRIPTION_YEARLY_PLAN_ID = "premium-mode-yearly-plan"
    }
}