package com.erdees.foodcostcalc.ext

import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetails.SubscriptionOfferDetails
import com.erdees.foodcostcalc.domain.model.premiumSubscription.Plan
import com.erdees.foodcostcalc.domain.model.premiumSubscription.PremiumSubscription
import com.erdees.foodcostcalc.utils.billing.PremiumUtil

/**
 * Maps ProductDetails to PremiumSubscription.
 *
 * @throws IllegalStateException if monthly or yearly plan not found.
 * */
fun ProductDetails.toPremiumSubscription(): PremiumSubscription {
    val monthlyPlan =
        this.subscriptionOfferDetails?.find {
            it.basePlanId == PremiumUtil.SUBSCRIPTION_MONTHLY_PLAN_ID
        }?.toPlan() ?: throw IllegalStateException("Monthly plan not found")
    val yearlyPlan =
        this.subscriptionOfferDetails?.find {
            it.basePlanId == PremiumUtil.SUBSCRIPTION_YEARLY_PLAN_ID
        }?.toPlan() ?: throw IllegalStateException("Yearly plan not found")
    return PremiumSubscription(
        id = this.productId,
        title = this.name,
        description = this.description,
        monthlyPlan = monthlyPlan,
        yearlyPlan = yearlyPlan
    )
}

/**
 * Maps SubscriptionOfferDetails to Plan.
 *
 * @throws NoSuchElementException if pricingPhases is empty.
 * */
fun SubscriptionOfferDetails.toPlan(): Plan {
    return Plan(
        id = this.basePlanId,
        offerIdToken = this.offerToken,
        billingPeriod = this.pricingPhases.pricingPhaseList.first().billingPeriod,
        formattedPrice = this.pricingPhases.pricingPhaseList.first().formattedPrice,
        currencyCode = this.pricingPhases.pricingPhaseList.first().priceCurrencyCode,
        priceInMicros = this.pricingPhases.pricingPhaseList.first().priceAmountMicros
    )
}