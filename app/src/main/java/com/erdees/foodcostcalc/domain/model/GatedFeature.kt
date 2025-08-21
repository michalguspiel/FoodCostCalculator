package com.erdees.foodcostcalc.domain.model

/**
 * Represents premium features that are gated behind a subscription.
 * These features are available to premium subscribers or grandfathered users.
 */
sealed class GatedFeature {
    /**
     * Cloud synchronization feature for backing up and syncing data across devices.
     */
    data object CloudSync : GatedFeature()
    
    /**
     * PDF export feature for exporting recipes and cost calculations to PDF format.
     */
    data object PdfExport : GatedFeature()
    
    /**
     * Advanced analytics feature for detailed cost analysis and reporting.
     */
    data object AdvancedAnalytics : GatedFeature()
    
    /**
     * Unlimited recipe storage beyond the free tier limits.
     */
    data object UnlimitedRecipes : GatedFeature()
}