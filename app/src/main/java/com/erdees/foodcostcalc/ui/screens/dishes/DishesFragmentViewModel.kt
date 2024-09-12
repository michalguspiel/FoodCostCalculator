package com.erdees.foodcostcalc.ui.screens.dishes

import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.repository.DishRepository
import com.erdees.foodcostcalc.domain.mapper.Mapper.toDishDomain
import com.erdees.foodcostcalc.domain.model.ItemPresentationState
import com.erdees.foodcostcalc.domain.model.dish.DishDomain
import com.erdees.foodcostcalc.ui.tools.ListPresentationStateHandler
import com.erdees.foodcostcalc.ui.viewModel.FCCBaseViewModel
import com.erdees.foodcostcalc.utils.Constants
import com.erdees.foodcostcalc.utils.ads.ListAdsInjectorManager
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DishesFragmentViewModel : FCCBaseViewModel(), KoinComponent {

    private val dishRepository: DishRepository by inject()
    val listPresentationStateHandler = ListPresentationStateHandler { resetScreenState() }

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
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        emptyList()
    )

    @OptIn(FlowPreview::class)
    private val filteredDishes: StateFlow<List<DishDomain>> =
        combine(
            dishes,
            searchKey.debounce(500).onStart { emit("") }
        ) { dishes, searchKey ->
            dishes.filter {
                it.name.lowercase().contains(searchKey.lowercase())
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val filteredDishesInjectedWithAds =
        filteredDishes.map { dishes ->
            ListAdsInjectorManager(
                dishes,
                Constants.Ads.DISHES_AD_FREQUENCY
            ).listInjectedWithAds
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = listOf()
        )
}