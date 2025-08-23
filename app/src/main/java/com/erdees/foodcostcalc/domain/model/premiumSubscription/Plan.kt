package com.erdees.foodcostcalc.domain.model.premiumSubscription

/**
 * Represents a plan for a premium subscription.
 * */
data class Plan(
    val id : String,
    val offerIdToken: String,
    val billingPeriod: String,
    val formattedPrice: String,
    val currencyCode: String,
    val priceInMicros: Long,
)