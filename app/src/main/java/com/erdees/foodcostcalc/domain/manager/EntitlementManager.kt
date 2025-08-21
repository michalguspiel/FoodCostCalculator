package com.erdees.foodcostcalc.domain.manager

import com.erdees.foodcostcalc.data.Preferences
import com.erdees.foodcostcalc.data.repository.DishRepository
import com.erdees.foodcostcalc.data.repository.HalfProductRepository
import com.erdees.foodcostcalc.domain.model.GatedFeature
import com.erdees.foodcostcalc.utils.FeatureManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * Singleton manager that serves as the single source of truth for all monetization-related
 * business logic in the Food Cost Calculator app. This manager determines whether a user
 * is allowed to perform certain actions based on their subscription status, feature flags,
 * and current usage counts.
 */
class EntitlementManager(
    private val userRepository: Preferences,
    private val dishRepository: DishRepository,
    private val halfProductRepository: HalfProductRepository,
    private val featureCutOffManager: FeatureManager
) {
    
    companion object {
        /**
         * Maximum number of dishes allowed for free tier users.
         */
        private const val FREE_DISH_LIMIT = 20
        
        /**
         * Maximum number of half-products allowed for free tier users.
         */
        private const val FREE_HALF_PRODUCT_LIMIT = 2
    }
    
    /**
     * Determines if the user is allowed to create a new dish.
     * 
     * @return true if any of the following conditions are met:
     * - The user is a premium subscriber
     * - The user is a grandfathered user
     * - The user's current dish count is less than FREE_DISH_LIMIT
     * Otherwise, returns false.
     */
    suspend fun canCreateDish(): Boolean {
        // Check premium status first (most efficient)
        if (userRepository.userHasActiveSubscription().first()) {
            return true
        }
        
        // Check if user is grandfathered
        if (featureCutOffManager.isGrandfatheredUser()) {
            return true
        }
        
        // Check if user is under the free tier limit
        val currentDishCount = dishRepository.getDishCount()
        return currentDishCount < FREE_DISH_LIMIT
    }
    
    /**
     * Determines if the user is allowed to create a new half-product.
     * 
     * @return true if any of the following conditions are met:
     * - The user is a premium subscriber
     * - The user is a grandfathered user  
     * - The user's current half-product count is less than FREE_HALF_PRODUCT_LIMIT
     * Otherwise, returns false.
     */
    suspend fun canCreateHalfProduct(): Boolean {
        // Check premium status first (most efficient)
        if (userRepository.userHasActiveSubscription().first()) {
            return true
        }
        
        // Check if user is grandfathered
        if (featureCutOffManager.isGrandfatheredUser()) {
            return true
        }
        
        // Check if user is under the free tier limit
        val currentHalfProductCount = halfProductRepository.getHalfProductCount()
        return currentHalfProductCount < FREE_HALF_PRODUCT_LIMIT
    }
    
    /**
     * Determines if a specific "power feature" is available to the user.
     * 
     * @param feature The gated feature to check availability for
     * @return Flow<Boolean> that emits true if the user is a premium subscriber
     * or a legacy subscriber, false otherwise. This allows the UI to reactively 
     * show/hide "Premium" badges on features.
     */
    fun isFeatureUnlocked(feature: GatedFeature): Flow<Boolean> {
        return userRepository.userHasActiveSubscription()
    }

    /**
     * Determines if a specific "power feature" is available to the user (suspend version).
     * Checks both premium subscribers and legacy subscribers.
     * 
     * @param feature The gated feature to check availability for
     * @return true if the user has access to the feature (either premium or legacy subscriber)
     */
    suspend fun isFeatureUnlockedSuspend(feature: GatedFeature): Boolean {
        // Check if user has active premium subscription
        if (userRepository.userHasActiveSubscription().first()) {
            return true
        }
        
        // Check if user is a legacy subscriber (grandfathered + has subscription)
        return isLegacySubscriber()
    }

    /**
     * Determines if the user is a legacy subscriber who should get premium features for free.
     * Legacy subscribers are users who:
     * 1. Installed the app before the grandfathered cutoff date (early supporters)
     * 2. Have an active subscription (any subscription, including the old ad-free plan)
     * 
     * @return true if the user is a legacy subscriber who should get free premium access
     */
    suspend fun isLegacySubscriber(): Boolean {
        // Must be grandfathered (installed before cutoff) AND have active subscription
        return featureCutOffManager.isGrandfatheredUser() && 
               userRepository.userHasActiveSubscription().first()
    }
}