package com.erdees.foodcostcalc.ui.screens.halfProducts

import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.Preferences
import com.erdees.foodcostcalc.data.model.local.HalfProductBase
import com.erdees.foodcostcalc.data.repository.AnalyticsRepository
import com.erdees.foodcostcalc.data.repository.HalfProductRepository
import com.erdees.foodcostcalc.domain.mapper.Mapper.toHalfProductDomain
import com.erdees.foodcostcalc.domain.model.InteractionType
import com.erdees.foodcostcalc.domain.model.ItemPresentationState
import com.erdees.foodcostcalc.domain.model.ScreenState
import com.erdees.foodcostcalc.domain.model.halfProduct.HalfProductDomain
import com.erdees.foodcostcalc.domain.model.units.MeasurementUnit
import com.erdees.foodcostcalc.ui.tools.ListPresentationStateHandler
import com.erdees.foodcostcalc.ui.viewModel.FCCBaseViewModel
import com.erdees.foodcostcalc.utils.Constants
import com.erdees.foodcostcalc.utils.Utils
import com.erdees.foodcostcalc.utils.ads.ListAdsInjectorManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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

    val currency = preferences.currency.stateIn(viewModelScope, SharingStarted.Lazily, null)
    private val imperialUsed =
        preferences.imperialUsed.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    private val metricUsed =
        preferences.metricUsed.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val adFrequency: StateFlow<Int> =
        preferences.userHasActiveSubscription().map { hasSubscription ->
            if (hasSubscription) Constants.Ads.PREMIUM_FREQUENCY
            else Constants.Ads.HALF_PRODUCTS_AD_FREQUENCY
        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            Constants.Ads.HALF_PRODUCTS_AD_FREQUENCY
        )


    val listPresentationStateHandler = ListPresentationStateHandler { resetScreenState() }

    private val halfProducts: StateFlow<List<HalfProductDomain>?> =
        halfProductRepository.completeHalfProducts.map { halfProducts ->
            halfProducts.map { it.toHalfProductDomain() }.also { halfProductsDomain ->
                // Initialize presentation state for each item
                // Perhaps a nice improvement would be to check if the item is already in the map
                // and only initialize it if it's not
                // If it's in the map and not in the list, it means it was deleted.
                // Therefore, it should be removed from itemPresentationState map. To be done in 3.1

                listPresentationStateHandler.updatePresentationState(halfProductsDomain.associate {
                    it.id to ItemPresentationState(
                        quantity = it.totalQuantity
                    )
                })
            }
        }.stateIn(
            scope = viewModelScope, started = SharingStarted.Lazily, initialValue = null
        )

    private val filteredHalfProducts = combine(
        halfProducts, debouncedSearch.onStart { emit("") }) { halfProducts, searchKey ->
        halfProducts?.filter {
            it.name.lowercase(Locale.getDefault()).contains(searchKey.lowercase())
        }
    }.stateIn(
        scope = viewModelScope, started = SharingStarted.Lazily, initialValue = null
    )

    val filteredHalfProductsInjectedWithAds =
        combine(filteredHalfProducts, adFrequency) { halfProducts, adFrequency ->
            halfProducts?.let {
                ListAdsInjectorManager(
                    halfProducts,
                    adFrequency
                ).listInjectedWithAds
            }
        }.stateIn(
            scope = viewModelScope, started = SharingStarted.Lazily, initialValue = null
        )

    val isEmptyListContentVisible: StateFlow<Boolean> = halfProducts.map { halfProducts ->
        halfProducts?.isEmpty() == true
    }.stateIn(
        scope = viewModelScope, started = SharingStarted.Lazily, initialValue = false
    )


    fun addHalfProduct(name: String, unit: MeasurementUnit) {
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

    fun onEditQuantity(id: Long) {
        analyticsRepository.logEvent(Constants.Analytics.Buttons.HALF_PRODUCTS_EDIT_QUANTITY, null)
        updateScreenState(
            ScreenState.Interaction(
                InteractionType.EditQuantity(id)
            )
        )
    }

    fun onAdFailedToLoad() {
        analyticsRepository.logEvent(Constants.Analytics.AD_FAILED_TO_LOAD, null)
    }

    fun getUnitsSet(): Set<MeasurementUnit> {
        return Utils.getUnitsSet(metricUsed.value, imperialUsed.value)
    }
}