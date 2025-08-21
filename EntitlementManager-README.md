# EntitlementManager Implementation

This document describes the EntitlementManager implementation for the Food Cost Calculator app.

## Overview

The `EntitlementManager` is a singleton class that serves as the single source of truth for all monetization-related business logic. It determines whether a user is allowed to perform certain actions based on their subscription status, feature flags, and current usage counts.

## Architecture

### Dependencies
- **UserRepository** (Preferences): Provides subscription status via `userHasActiveSubscription(): Flow<Boolean>`
- **DishRepository**: Provides dish count via `getDishCount(): Int`
- **HalfProductRepository**: Provides half-product count via `getHalfProductCount(): Int`
- **FeatureCutOffManager** (FeatureManager): Provides grandfathering logic via `isGrandfatheredUser(): Boolean`

### Constants
- `FREE_DISH_LIMIT = 20`: Maximum dishes allowed for free users
- `FREE_HALF_PRODUCT_LIMIT = 2`: Maximum half-products allowed for free users

## Public API

### `canCreateDish(): Boolean`
Determines if the user can create a new dish. Returns `true` if:
- User has active subscription, OR
- User is grandfathered, OR  
- Current dish count < FREE_DISH_LIMIT

### `canCreateHalfProduct(): Boolean`
Determines if the user can create a new half-product. Returns `true` if:
- User has active subscription, OR
- User is grandfathered, OR
- Current half-product count < FREE_HALF_PRODUCT_LIMIT

### `isFeatureUnlocked(feature: GatedFeature): Flow<Boolean>`
Returns a reactive flow indicating if a premium feature is unlocked. Currently returns subscription status for all gated features.

## Gated Features

The `GatedFeature` sealed class defines premium features:
- `CloudSync`: Cloud synchronization
- `PdfExport`: PDF export functionality
- `AdvancedAnalytics`: Advanced analytics and reporting
- `UnlimitedRecipes`: Unlimited recipe storage

## Usage Example

```kotlin
class DishListViewModel : ViewModel(), KoinComponent {
    private val entitlementManager: EntitlementManager by inject()

    fun onAddDishClicked() {
        viewModelScope.launch {
            if (entitlementManager.canCreateDish()) {
                // Navigate to Create Dish screen
            } else {
                // Show paywall overlay
            }
        }
    }
}
```

## Grandfathering Logic

Users who installed the app before December 1, 2024 are considered "grandfathered" and receive free access to premium features. This is managed by the `FeatureManager.isGrandfatheredUser()` method.

## Dependency Injection

The EntitlementManager is registered as a singleton in the `utilModule`:

```kotlin
single<EntitlementManager> {
    EntitlementManager(
        userRepository = get(),
        dishRepository = get(),
        halfProductRepository = get(),
        featureCutOffManager = get()
    )
}
```

## Testing

Comprehensive unit tests cover all business logic scenarios:
- Premium user access
- Grandfathered user access
- Free tier limits
- Feature unlocking logic

Run tests with: `./gradlew testDebugUnitTest`