package com.erdees.foodcostcalc.ui.screens.dishes.forms.componentlookup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erdees.foodcostcalc.data.repository.AnalyticsRepository
import com.erdees.foodcostcalc.data.repository.HalfProductRepository
import com.erdees.foodcostcalc.data.repository.ProductRepository
import com.erdees.foodcostcalc.domain.mapper.Mapper.toHalfProductDomain
import com.erdees.foodcostcalc.domain.mapper.Mapper.toProductDomain
import com.erdees.foodcostcalc.domain.model.Item
import com.erdees.foodcostcalc.domain.model.halfProduct.HalfProductDomain
import com.erdees.foodcostcalc.domain.model.product.ProductDomain
import com.erdees.foodcostcalc.ui.screens.dishes.createDishV2.DishCreationAnalyticsHelper
import com.erdees.foodcostcalc.utils.Constants.UI.SEARCH_DEBOUNCE_MS
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.Lazily
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Locale

data class ComponentLookupResult(
    val products: List<ProductDomain> = emptyList(),
    val halfProducts: List<HalfProductDomain> = emptyList()
) {
    val isEmpty: Boolean
        get() = products.isEmpty() && halfProducts.isEmpty()
}

class ComponentLookupViewModel : ViewModel(), KoinComponent {

    private val analyticsRepository: AnalyticsRepository by inject()
    private val productRepository: ProductRepository by inject()
    private val halfProductRepository: HalfProductRepository by inject()
    private val analyticsHelper = DishCreationAnalyticsHelper(analyticsRepository)

    private val suggestionsManuallyDismissed = MutableStateFlow(false)
    private val _newComponentName = MutableStateFlow("")
    val newComponentName = _newComponentName
    private val selectedComponent = MutableStateFlow<Item?>(null)

    val products: StateFlow<List<ProductDomain>> =
        productRepository.products.map { list ->
            list.map { it.toProductDomain() }
        }.stateIn(
            viewModelScope,
            Lazily,
            listOf()
        )

    val halfProducts: StateFlow<List<HalfProductDomain>> =
        halfProductRepository.halfProducts.map { list ->
            list.map { it.toHalfProductDomain() }
        }.stateIn(
            viewModelScope,
            Lazily,
            listOf()
        )

    @OptIn(FlowPreview::class)
    val suggestedComponents: StateFlow<ComponentLookupResult?> =
        combine(
            products,
            halfProducts,
            newComponentName.debounce(SEARCH_DEBOUNCE_MS)
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
            scope = viewModelScope, started = Lazily, initialValue = ComponentLookupResult()
        )

    @OptIn(FlowPreview::class)
    val shouldShowSuggestedProducts = combine(
        newComponentName.debounce(SEARCH_DEBOUNCE_MS),
        suggestedComponents,
        selectedComponent,
        suggestionsManuallyDismissed
    ) { newProductName, suggestedProducts, selectedSuggestedProduct, suggestionsManuallyDismissed ->
        newProductName.isNotBlank() &&
                newProductName.length > 2 &&
                suggestedProducts?.isEmpty == false &&
                selectedSuggestedProduct == null &&
                !suggestionsManuallyDismissed
    }.stateIn(
        scope = viewModelScope, started = Lazily, initialValue = false
    )

    // Flag to indicate whether "Create new" option should be shown
    val shouldShowCreateNew = _newComponentName.map { name ->
        name.isNotBlank() && name.length > 2 &&
                !products.value.any { it.name.equals(name, ignoreCase = true) } &&
                !halfProducts.value.any { it.name.equals(name, ignoreCase = true) }
    }.stateIn(
        scope = viewModelScope, started = Lazily, initialValue = false
    )

    fun onSuggestionsManuallyDismissed() {
        suggestionsManuallyDismissed.value = true
        analyticsHelper.logSuggestionDismissed()
    }

    fun updateNewComponentName(newValue: String) {
        suggestionsManuallyDismissed.value = false
        _newComponentName.value = newValue
        updateSelectedComponent(newValue)
    }

    private fun updateSelectedComponent(newValue: String) {
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

    fun onCreateNewComponent() {
        // Todo log
        // Logic to handle creation of new component based on the current name
        // Additional logic for component creation would go here
    }
}

class ComponentLookupForm {
}