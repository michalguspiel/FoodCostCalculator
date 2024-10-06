package com.erdees.foodcostcalc.ui.screens.halfProducts

import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.Preferences
import com.erdees.foodcostcalc.data.model.HalfProductBase
import com.erdees.foodcostcalc.data.repository.AnalyticsRepository
import com.erdees.foodcostcalc.data.repository.HalfProductRepository
import com.erdees.foodcostcalc.domain.mapper.Mapper.toHalfProductDomain
import com.erdees.foodcostcalc.domain.model.ItemPresentationState
import com.erdees.foodcostcalc.domain.model.halfProduct.HalfProductDomain
import com.erdees.foodcostcalc.ui.tools.ListPresentationStateHandler
import com.erdees.foodcostcalc.ui.viewModel.FCCBaseViewModel
import com.erdees.foodcostcalc.utils.Constants
import com.erdees.foodcostcalc.utils.ads.ListAdsInjectorManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Locale

class HalfProductsScreenViewModel : FCCBaseViewModel(), KoinComponent {

    private val halfProductRepository: HalfProductRepository by inject()
    private val analyticsRepository: AnalyticsRepository by inject()
    val preferences: Preferences by inject()

    val listPresentationStateHandler = ListPresentationStateHandler { resetScreenState() }

    private val halfProducts: StateFlow<List<HalfProductDomain>> =
        halfProductRepository.completeHalfProducts.map { halfProducts ->
            halfProducts.map { it.toHalfProductDomain() }.also { halfProductsDomain ->
                // Initialize presentation state for each item
                // Perhaps a nice improvement would be to check if the item is already in the map
                // and only initialize it if it's not
                // If it's in the map and not in the list, it means it was deleted.
                // Therefore, it should be removed from itemPresentationState map. To be done in 3.1

                listPresentationStateHandler.updatePresentationState(
                    halfProductsDomain.associate { it.id to ItemPresentationState(quantity = it.totalQuantity) }
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )

    @OptIn(FlowPreview::class)
    private val filteredHalfProducts = combine(
        halfProducts,
        searchKey.debounce(500).onStart { emit("") }
    ) { halfProducts, searchKey ->
        halfProducts.filter {
            it.name.lowercase(Locale.getDefault()).contains(searchKey.lowercase())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = emptyList()
    )

    val filteredHalfProductsInjectedWithAds =
        filteredHalfProducts.map { halfProducts ->
            ListAdsInjectorManager(
                halfProducts,
                Constants.Ads.HALF_PRODUCTS_AD_FREQUENCY
            ).listInjectedWithAds
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = listOf()
        )

    fun addHalfProduct(name: String, unit: String) {
        val halfProductBase = HalfProductBase(0, name, unit)
        with(halfProductBase) {
            addHalfProduct(this)
            analyticsRepository.logEvent(Constants.Analytics.HALF_PRODUCT_CREATED, null)
        }
        resetScreenState()
    }

    private fun addHalfProduct(halfProductBase: HalfProductBase) {
        viewModelScope.launch(Dispatchers.IO) {
            halfProductRepository.addHalfProduct(halfProductBase)
        }
    }

    fun onAdFailedToLoad() {
        analyticsRepository.logEvent(Constants.Analytics.AD_FAILED_TO_LOAD, null)
    }
}