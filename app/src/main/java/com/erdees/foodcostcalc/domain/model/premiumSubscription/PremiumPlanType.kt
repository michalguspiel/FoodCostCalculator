package com.erdees.foodcostcalc.domain.model.premiumSubscription

enum class PremiumPlanType(
    val productId: String,
    val monthlyId: String,
    val yearlyId: String,
) {
    LEGACY(
        "food.cost.calculator.premium.account",
        "premium-mode-monthly-plan",
        "premium-mode-yearly-plan",
    ),
    UNLIMITED_PREMIUM(
        "food.cost.calculator.unlimited.premium",
        "unlimited-premium-monthly-plan",
        "unlimited-premium-yearly-plan",
    );

    companion object {
        fun fromId(productId: String): PremiumPlanType {
            return PremiumPlanType.entries.first { it.productId == productId }
        }
    }
}
