package com.erdees.foodcostcalc.ui.screens.dishes

import android.os.Bundle
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.Preferences
import com.erdees.foodcostcalc.data.repository.AnalyticsRepository
import com.erdees.foodcostcalc.data.repository.DishRepository
import com.erdees.foodcostcalc.domain.mapper.Mapper.toDishDomain
import com.erdees.foodcostcalc.domain.model.InteractionType
import com.erdees.foodcostcalc.domain.model.ItemPresentationState
import com.erdees.foodcostcalc.domain.model.ScreenState
import com.erdees.foodcostcalc.domain.model.dish.DishDomain
import com.erdees.foodcostcalc.ui.spotlight.Spotlight
import com.erdees.foodcostcalc.ui.tools.ListPresentationStateHandler
import com.erdees.foodcostcalc.ui.viewModel.FCCBaseViewModel
import com.erdees.foodcostcalc.utils.Constants
import com.erdees.foodcostcalc.utils.ads.ListAdsInjectorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DishesScreenViewModel : FCCBaseViewModel(), KoinComponent {

    private val dishRepository: DishRepository by inject()
    private val analyticsRepository: AnalyticsRepository by inject()
    private val preferences: Preferences by inject()
    val spotlight: Spotlight by inject()

    val currency = preferences.currency.stateIn(viewModelScope, SharingStarted.Lazily, null)
    val listPresentationStateHandler = ListPresentationStateHandler { resetScreenState() }

    private val _askForReview: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val askForReview = _askForReview

    private val adFrequency: StateFlow<Int> = preferences.userHasActiveSubscription()
        .map { hasSubscription ->
            if (hasSubscription) Constants.Ads.PREMIUM_FREQUENCY
            else Constants.Ads.DISHES_AD_FREQUENCY
        }.stateIn(viewModelScope, SharingStarted.Eagerly, Constants.Ads.DISHES_AD_FREQUENCY)

    private val dishes = dishRepository.dishes.map { dishes ->
        dishes.map { dish -> dish.toDishDomain() }.also { dishesDomain ->
            // Initialize presentation state for each item
            // Perhaps a nice improvement would be to check if the item is already in the map
            // and only initialize it if it's not
            // If it's in the map and not in the list, it means it was deleted.
            // Therefore, it should be removed from itemPresentationState map. To be done in 3.1
            listPresentationStateHandler.updatePresentationState(
                dishesDomain.associate { it.id to ItemPresentationState() }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    private val filteredDishes: StateFlow<List<DishDomain>?> =
        combine(
            dishes, debouncedSearch.onStart { emit("") }
        ) { dishes, searchKey ->
            dishes?.filter {
                it.name.lowercase().contains(searchKey.lowercase())
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    val filteredDishesInjectedWithAds =
        combine(filteredDishes, adFrequency) { dishes, freq ->
            dishes?.let { ListAdsInjectorManager(it, freq).listInjectedWithAds }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = null
        )

    val isEmptyListContentVisible: StateFlow<Boolean> = dishes.map { dishes ->
        dishes?.isEmpty() == true
    }.stateIn(
        scope = viewModelScope, started = SharingStarted.Lazily, initialValue = false
    )

    fun onAdFailedToLoad() {
        analyticsRepository.logEvent(Constants.Analytics.AD_FAILED_TO_LOAD, null)
    }

    fun onChangeServingsClick(id: Long) {
        analyticsRepository.logEvent(
            Constants.Analytics.Buttons.DISHES_EDIT_DISPLAYED_PORTIONS,
            null
        )
        updateScreenState(
            ScreenState.Interaction(
                InteractionType.EditQuantity(id)
            )
        )
    }

    fun userCanBeAskedForReview() {
        _askForReview.update { true }
    }

    fun reviewSuccess() {
        analyticsRepository.logEvent(Constants.Analytics.REVIEW_SUCCESS, null)
    }

    fun reviewFailure(exception: Throwable) {
        val bundle = Bundle().apply {
            putString(Constants.Analytics.REVIEW_FAILURE, null)
        }
        analyticsRepository.logException(exception, bundle)
    }
}