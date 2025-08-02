package com.erdees.foodcostcalc.ui.screens.dishes.forms.componentlookup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.repository.HalfProductRepository
import com.erdees.foodcostcalc.data.repository.ProductRepository
import com.erdees.foodcostcalc.domain.mapper.Mapper.toHalfProductDomain
import com.erdees.foodcostcalc.domain.mapper.Mapper.toProductDomain
import com.erdees.foodcostcalc.domain.model.Item
import com.erdees.foodcostcalc.domain.model.halfProduct.HalfProductDomain
import com.erdees.foodcostcalc.domain.model.product.ProductDomain
import com.erdees.foodcostcalc.ui.screens.dishes.DishAnalyticsHelper
import com.erdees.foodcostcalc.utils.Constants
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
import org.koin.core.component.get
import org.koin.core.component.inject
import timber.log.Timber
import java.util.Locale

class ComponentLookupViewModel : ViewModel(), KoinComponent {

    private val productRepository: ProductRepository by inject()
    private val halfProductRepository: HalfProductRepository by inject()
    private val analyticsHelper = DishAnalyticsHelper(get())

    private val suggestionsManuallyDismissed = MutableStateFlow(false)
    private val _newComponentName = MutableStateFlow("")

    val newComponentName: StateFlow<String> = _newComponentName
    val selectedComponent = MutableStateFlow<Item?>(null)

    private val products: StateFlow<List<ProductDomain>> =
        productRepository.products.map { list ->
            list.map { it.toProductDomain() }
        }.stateIn(
            viewModelScope,
            SharingStarted.Companion.Lazily,
            listOf()
        )

    private val halfProducts: StateFlow<List<HalfProductDomain>> =
        halfProductRepository.completeHalfProducts.map { list ->
            list.map { it.toHalfProductDomain() }
        }.stateIn(
            viewModelScope,
            SharingStarted.Companion.Lazily,
            listOf()
        )

    @OptIn(FlowPreview::class)
    val suggestedComponents: StateFlow<ComponentLookupResult> =
        combine(
            products,
            halfProducts,
            newComponentName.debounce(Constants.UI.SEARCH_DEBOUNCE_MS)
                .onStart { emit("") }) { products, halfProducts, searchWord ->
            ComponentLookupResult(
                products = products.filter {
                    it.name.lowercase(Locale.getDefault()).contains(searchWord.lowercase())
                },
                halfProducts = halfProducts.filter {
                    it.name.lowercase(Locale.getDefault()).contains(searchWord.lowercase())
                }
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.Lazily,
            initialValue = ComponentLookupResult()
        )

    @OptIn(FlowPreview::class)
    val shouldShowSuggestedProducts = combine(
        newComponentName.debounce(Constants.UI.SEARCH_DEBOUNCE_MS),
        suggestedComponents,
        selectedComponent,
    ) { newProductName, suggestedProducts, selectedSuggestedProduct ->
        newProductName.isNotBlank() &&
                newProductName.length > 2 &&
                !suggestedProducts.isEmpty &&
                selectedSuggestedProduct == null
    }.stateIn(
        scope = viewModelScope, started = SharingStarted.Companion.Lazily, initialValue = false
    )

    val uiState: StateFlow<ComponentLookupFormUiState> = combine(
        suggestedComponents,
        shouldShowSuggestedProducts,
        newComponentName,
        selectedComponent
    ) { suggestedComponents, showSuggestedComponents, newComponentName, selectedComponent ->
        ComponentLookupFormUiState(
            suggestedComponents = suggestedComponents,
            showSuggestedComponents = showSuggestedComponents,
            newComponentName = newComponentName,
            selectedComponent = selectedComponent
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Companion.Lazily,
        initialValue = ComponentLookupFormUiState()
    )

    fun onComponentSelected(item: Item) {
        Timber.i("onComponentSelected: ${item.name}")
        _newComponentName.value = item.name
        selectedComponent.value = item
        analyticsHelper.logSuggestionSelected(item.name)
    }

    fun updateNewComponentName(newValue: String) {
        Timber.i("updateNewComponentName: $newValue")
        suggestionsManuallyDismissed.value = false
        _newComponentName.value = newValue
        updateSelectedComponent(newValue)
    }

    private fun updateSelectedComponent(newValue: String) {
        Timber.i("updateSelectedComponent: $newValue")
        if (newValue.isBlank()) {
            selectedComponent.value = null
            return
        }

        products.value.find { it.name == newValue }?.let {
            selectedComponent.value = it
            return
        }

        halfProducts.value.find { it.name == newValue }?.let {
            selectedComponent.value = it
            return
        }

        selectedComponent.value = null
    }

    fun getComponentSelectionResult(): ComponentSelection {
        return selectedComponent.value?.let { ComponentSelection.ExistingComponent(it) }
            ?: ComponentSelection.NewComponent(newComponentName.value)
    }

    fun reset() {
        _newComponentName.value = ""
        selectedComponent.value = null
    }
}
