package com.erdees.foodcostcalc.domain.model.premiumSubscription

/**
 * Premium Subscription, currently removes ads.
 * */
data class PremiumSubscription(
    val id: String,
    val title: String,
    val description: String,
    val monthlyPlan: Plan,
    val yearlyPlan: Plan
)