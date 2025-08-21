package com.erdees.foodcostcalc.example

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.domain.manager.EntitlementManager
import com.erdees.foodcostcalc.domain.model.GatedFeature
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Example usage of EntitlementManager in a ViewModel
 * This demonstrates how the EntitlementManager would be used in practice
 */
class ExampleDishListViewModel : ViewModel(), KoinComponent {

    private val entitlementManager: EntitlementManager by inject()

    /**
     * Example: In the DishListViewModel, when the user taps the '+' FAB
     */
    fun onAddDishClicked() {
        viewModelScope.launch {
            if (entitlementManager.canCreateDish()) {
                // Navigate to Create Dish screen
                navigateToCreateDish()
            } else {
                // Show the "Limit Reached" paywall overlay
                showPaywallOverlay()
            }
        }
    }

    /**
     * Example: When user tries to create a half-product
     */
    fun onAddHalfProductClicked() {
        viewModelScope.launch {
            if (entitlementManager.canCreateHalfProduct()) {
                // Navigate to Create Half Product screen
                navigateToCreateHalfProduct()
            } else {
                // Show the "Limit Reached" paywall overlay for half products
                showHalfProductPaywallOverlay()
            }
        }
    }

    /**
     * Example: Check if a premium feature should be visible to the user
     */
    fun observeCloudSyncFeature() {
        viewModelScope.launch {
            entitlementManager.isFeatureUnlocked(GatedFeature.CloudSync).collect { isUnlocked ->
                if (isUnlocked) {
                    // Show cloud sync option in UI
                    showCloudSyncOption()
                } else {
                    // Show "Premium" badge on cloud sync feature
                    showPremiumBadge()
                }
            }
        }
    }

    /**
     * Example: Check if PDF export should be available
     */
    fun onExportToPdfClicked() {
        viewModelScope.launch {
            entitlementManager.isFeatureUnlocked(GatedFeature.PdfExport).collect { isUnlocked ->
                if (isUnlocked) {
                    // Start PDF export process
                    startPdfExport()
                } else {
                    // Show premium upgrade prompt
                    showPremiumUpgradePrompt()
                }
            }
        }
    }

    // Mock implementations for demonstration
    private fun navigateToCreateDish() {}
    private fun showPaywallOverlay() {}
    private fun navigateToCreateHalfProduct() {}
    private fun showHalfProductPaywallOverlay() {}
    private fun showCloudSyncOption() {}
    private fun showPremiumBadge() {}
    private fun startPdfExport() {}
    private fun showPremiumUpgradePrompt() {}
}