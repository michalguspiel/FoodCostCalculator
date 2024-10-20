package com.erdees.foodcostcalc.ui.screens.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.Preferences
import com.erdees.foodcostcalc.data.repository.AnalyticsRepository
import com.erdees.foodcostcalc.data.repository.ProductRepository
import com.erdees.foodcostcalc.domain.mapper.Mapper.toProductDomain
import com.erdees.foodcostcalc.utils.Constants
import com.erdees.foodcostcalc.utils.ads.ListAdsInjectorManager
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Locale

class ProductsFragmentViewModel : ViewModel(), KoinComponent {

    private val productRepository: ProductRepository by inject()
    private val analyticsRepository: AnalyticsRepository by inject()
    private val preferences: Preferences by inject()

    private val adFrequency =
        if (preferences.userHasActiveSubscription) Constants.Ads.PREMIUM_FREQUENCY
        else Constants.Ads.PRODUCTS_AD_FREQUENCY

    private val products = productRepository.products.map { products ->
        products.map { it.toProductDomain() }
    }.stateIn(
        scope = viewModelScope, started = SharingStarted.Lazily, initialValue = emptyList()
    )

    private var _searchKey: MutableStateFlow<String> = MutableStateFlow("")
    val searchKey: StateFlow<String> = _searchKey

    fun updateSearchKey(searchKey: String) {
        _searchKey.value = searchKey
    }

    @OptIn(FlowPreview::class)
    private val filteredProducts =
        combine(products, searchKey.debounce(500).onStart { emit("") }) { products, searchWord ->
            products.filter {
                it.name.lowercase(Locale.getDefault()).contains(searchWord.lowercase())
            }
        }.stateIn(
            scope = viewModelScope, started = SharingStarted.Lazily, initialValue = emptyList()
        )

    val filteredProductsInjectedWithAds = filteredProducts.map {
        ListAdsInjectorManager(it, adFrequency).listInjectedWithAds
    }.stateIn(
        scope = viewModelScope, started = SharingStarted.Lazily, initialValue = listOf()
    )

    fun onAdFailedToLoad() {
        analyticsRepository.logEvent(Constants.Analytics.AD_FAILED_TO_LOAD, null)
    }
}