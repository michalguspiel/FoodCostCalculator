package com.erdees.foodcostcalc.ui.screens.dishes.forms.componentlookup

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.data.repository.AnalyticsRepository
import com.erdees.foodcostcalc.data.repository.HalfProductRepository
import com.erdees.foodcostcalc.data.repository.ProductRepository
import com.erdees.foodcostcalc.domain.mapper.Mapper.toHalfProductDomain
import com.erdees.foodcostcalc.domain.mapper.Mapper.toProductDomain
import com.erdees.foodcostcalc.domain.model.Item
import com.erdees.foodcostcalc.domain.model.halfProduct.HalfProductDomain
import com.erdees.foodcostcalc.domain.model.product.ProductDomain
import com.erdees.foodcostcalc.ui.composables.buttons.FCCPrimaryButton
import com.erdees.foodcostcalc.ui.composables.dividers.FCCPrimaryHorizontalDivider
import com.erdees.foodcostcalc.ui.composables.fields.FCCTextField
import com.erdees.foodcostcalc.ui.screens.dishes.createDishV2.DishCreationAnalyticsHelper
import com.erdees.foodcostcalc.ui.theme.FCCTheme
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

    val newComponentName: StateFlow<String> = _newComponentName
    val selectedComponent = MutableStateFlow<Item?>(null)

    private val products: StateFlow<List<ProductDomain>> =
        productRepository.products.map { list ->
            list.map { it.toProductDomain() }
        }.stateIn(
            viewModelScope,
            Lazily,
            listOf()
        )

    private val halfProducts: StateFlow<List<HalfProductDomain>> =
        halfProductRepository.halfProducts.map { list ->
            list.map { it.toHalfProductDomain() }
        }.stateIn(
            viewModelScope,
            Lazily,
            listOf()
        )

    @OptIn(FlowPreview::class)
    val suggestedComponents: StateFlow<ComponentLookupResult> =
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
                !suggestedProducts.isEmpty &&
                selectedSuggestedProduct == null &&
                !suggestionsManuallyDismissed
    }.stateIn(
        scope = viewModelScope, started = Lazily, initialValue = false
    )

    fun onComponentSelected(item: Item) {
        _newComponentName.value = item.name
        selectedComponent.value = item
        analyticsHelper.logSuggestionSelected(item.name)
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

    fun getComponentSelectionResult(): ComponentSelection {
        return selectedComponent.value?.let { ComponentSelection.ExistingComponent(it) }
            ?: ComponentSelection.NewComponent(newComponentName.value)
    }
}


sealed class ComponentSelection {
    data class ExistingComponent(val item: Item) : ComponentSelection()
    data class NewComponent(val name: String) : ComponentSelection()
}

@Composable
fun ComponentLookupForm(
    modifier: Modifier = Modifier,
    viewModel: ComponentLookupViewModel = viewModel(),
    onNext: (ComponentSelection) -> Unit
) {
    val suggestedComponents by viewModel.suggestedComponents.collectAsStateWithLifecycle()
    val showSuggestedComponents by viewModel.shouldShowSuggestedProducts.collectAsStateWithLifecycle()
    val newComponentName by viewModel.newComponentName.collectAsStateWithLifecycle()
    val selectedComponent by viewModel.selectedComponent.collectAsStateWithLifecycle()
    ComponentLookupFormContent(
        suggestedComponents = suggestedComponents,
        showSuggestedComponents = showSuggestedComponents,
        newComponentName = newComponentName,
        selectedComponent = selectedComponent,
        onNewComponentNameChange = viewModel::updateNewComponentName,
        onComponentSelected = viewModel::onComponentSelected,
        onNext = {
            onNext(viewModel.getComponentSelectionResult())
        },
        modifier = modifier,
    )
}

@Composable
private fun ComponentLookupFormContent(
    suggestedComponents: ComponentLookupResult,
    showSuggestedComponents: Boolean,
    newComponentName: String,
    selectedComponent: Item?,
    onNewComponentNameChange: (String) -> Unit,
    onComponentSelected: (Item) -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth()
            .heightIn(max = LocalWindowInfo.current.containerSize.height.dp * 0.6f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(stringResource(R.string.add_component), style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        FCCTextField(
            modifier = Modifier.fillMaxWidth(),
            title = "Search or create component",
            placeholder = "Component name",
            value = newComponentName,
            onValueChange = onNewComponentNameChange
        )
        FCCPrimaryHorizontalDivider()
        LazyColumn {
            if (showSuggestedComponents) {
                if (suggestedComponents.products.isNotEmpty()) {
                    item {
                        SuggestionListHeader("Matching ingredients")
                    }
                    items(suggestedComponents.products) {
                        SuggestionItem(
                            headlineText = it.name,
                            painter = painterResource(R.drawable.shopping_basket_24px),
                        ) { onComponentSelected(it) }
                    }
                }
                if (suggestedComponents.halfProducts.isNotEmpty()) {
                    item {
                        SuggestionListHeader("Matching ingredients")
                    }
                    items(suggestedComponents.halfProducts) {
                        SuggestionItem(
                            headlineText = it.name,
                            painter = painterResource(R.drawable.blender_24),
                        ) { onComponentSelected(it) }
                    }
                }
            } else {
                item {
                    val text = if (newComponentName.isBlank()) {
                        stringResource(R.string.start_typing_to_see_suggestions)
                    } else {
                        stringResource(R.string.no_suggestions, newComponentName)
                    }
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        val text = when (selectedComponent) {
            is ProductDomain -> stringResource(R.string.add_product)
            is HalfProductDomain -> stringResource(R.string.add_half_product)
            else -> stringResource(R.string.create_new_product)
        }
        FCCPrimaryButton(
            text, modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            enabled = newComponentName.isNotBlank()
        ) {
            onNext()
        }
    }
}

@Composable
fun SuggestionItem(
    painter: Painter,
    headlineText: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    ListItem(
        modifier = modifier.clickable { onClick() },
        headlineContent = {
            Text(
                text = headlineText,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        leadingContent = {
            Icon(
                painter = painter,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    )
}

@Composable
fun SuggestionListHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Preview
@Composable
private fun ComponentLookupFormPreview() {
    FCCTheme {
        ComponentLookupFormContent(
            suggestedComponents = ComponentLookupResult(
                products = listOf(
                    ProductDomain(
                        id = 1L,
                        name = "Flour",
                        pricePerUnit = 2.5,
                        tax = 8.0,
                        waste = 5.0,
                        unit = "kg"
                    ),
                    ProductDomain(
                        id = 2L,
                        name = "Sugar",
                        pricePerUnit = 3.0,
                        tax = 8.0,
                        waste = 3.0,
                        unit = "kg"
                    )
                ),
                halfProducts = listOf(
                    HalfProductDomain(
                        id = 1L,
                        name = "Dough",
                        halfProductUnit = "per kg",
                        products = emptyList()
                    )
                )
            ),
            showSuggestedComponents = true,
            newComponentName = "Fl",
            onNewComponentNameChange = {},
            onComponentSelected = {},
            onNext = {},
            selectedComponent = null,
            modifier = Modifier
        )
    }
}