package com.erdees.foodcostcalc.ui.screens.products

import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.Preferences
import com.erdees.foodcostcalc.data.repository.AnalyticsRepository
import com.erdees.foodcostcalc.data.repository.ProductRepository
import com.erdees.foodcostcalc.domain.mapper.Mapper.toProductDomain
import com.erdees.foodcostcalc.ui.viewModel.FCCBaseViewModel
import com.erdees.foodcostcalc.utils.Constants
import com.erdees.foodcostcalc.utils.ads.ListAdsInjectorManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Locale

class ProductsScreenViewModel : FCCBaseViewModel(), KoinComponent {

    private val productRepository: ProductRepository by inject()
    private val analyticsRepository: AnalyticsRepository by inject()
    private val preferences: Preferences by inject()

    val currency = preferences.currency.stateIn(viewModelScope, SharingStarted.Lazily, null)

    private val adFrequency: StateFlow<Int> = preferences.userHasActiveSubscription()
        .map { hasSubscription ->
            if (hasSubscription) Constants.Ads.PREMIUM_FREQUENCY
            else Constants.Ads.PRODUCTS_AD_FREQUENCY
        }.stateIn(viewModelScope, SharingStarted.Eagerly, Constants.Ads.PRODUCTS_AD_FREQUENCY)

    private val products = productRepository.products.map { products ->
        products.map { it.toProductDomain() }
    }.stateIn(
        scope = viewModelScope, started = SharingStarted.Lazily, initialValue = null
    )

    private val filteredProducts =
        combine(products, debouncedSearch.onStart { emit("") }) { products, searchWord ->
            products?.filter {
                it.name.lowercase(Locale.getDefault()).contains(searchWord.lowercase())
            }
        }.stateIn(
            scope = viewModelScope, started = SharingStarted.Lazily, initialValue = null
        )

    val filteredProductsInjectedWithAds =
        combine(filteredProducts, adFrequency) { filteredProducts, adFrequency ->
            filteredProducts?.let { products ->
                ListAdsInjectorManager(
                    products,
                    adFrequency
                ).listInjectedWithAds
            }
        }.stateIn(
            scope = viewModelScope, started = SharingStarted.Lazily, initialValue = null
        )

    val isEmptyListContentVisible: StateFlow<Boolean> =
        products.map { products -> products?.isEmpty() == true }.stateIn(
            scope = viewModelScope, started = SharingStarted.Lazily, initialValue = false
        )

    fun onAdFailedToLoad() {
        analyticsRepository.logEvent(Constants.Analytics.AD_FAILED_TO_LOAD, null)
    }
}