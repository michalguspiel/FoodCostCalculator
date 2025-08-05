package com.erdees.foodcostcalc.ui.screens.dishes.forms.componentlookup

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.KeyboardArrowRight
import androidx.compose.material.icons.sharp.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.domain.model.Item
import com.erdees.foodcostcalc.domain.model.halfProduct.HalfProductDomain
import com.erdees.foodcostcalc.domain.model.product.InputMethod
import com.erdees.foodcostcalc.domain.model.product.ProductDomain
import com.erdees.foodcostcalc.domain.model.units.MeasurementUnit
import com.erdees.foodcostcalc.ui.composables.buttons.FCCOutlinedButton
import com.erdees.foodcostcalc.ui.composables.dividers.FCCSecondaryHorizontalDivider
import com.erdees.foodcostcalc.ui.composables.fields.FCCTextField
import com.erdees.foodcostcalc.ui.theme.FCCTheme

@Composable
fun ComponentLookupForm(
    uiState: ComponentLookupFormUiState,
    actions: ComponentLookupFormActions,
    modifier: Modifier = Modifier,
) {
    ComponentLookupFormContent(
        suggestedComponents = uiState.suggestedComponents,
        showSuggestedComponents = uiState.showSuggestedComponents,
        newComponentName = uiState.newComponentName,
        selectedComponent = uiState.selectedComponent,
        onNewComponentNameChange = actions.onNewComponentNameChange,
        onSelectComponent = { item ->
            actions.onSelectComponent(item)
            actions.onNext()
        },
        onNext = actions.onNext,
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
    onSelectComponent: (Item) -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val wasComponentNameFocusRequested = rememberSaveable { mutableStateOf(false) }
    val componentNameFocusRequester = remember { FocusRequester() }
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
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(componentNameFocusRequester)
                .onGloballyPositioned{
                    if (!wasComponentNameFocusRequested.value) {
                        wasComponentNameFocusRequested.value = true
                        componentNameFocusRequester.requestFocus()
                    }
                }
            ,
            title = stringResource(R.string.search_or_create_component),
            placeholder = stringResource(R.string.search_or_create_component_hint),
            value = newComponentName,
            onValueChange = onNewComponentNameChange
        )
        LazyColumn(Modifier.weight(1f, fill = false)) {
            if (showSuggestedComponents) {
                if (suggestedComponents.products.isNotEmpty()) {
                    item {
                        SuggestionListHeader(stringResource(R.string.matching_ingredients))
                    }
                    itemsIndexed(suggestedComponents.products) { index, item ->
                        SuggestionItem(
                            headlineText = item.name,
                            painter = painterResource(R.drawable.shopping_basket_24px),
                        ) { onSelectComponent(item) }
                        if (index < suggestedComponents.products.size - 1) {
                            FCCSecondaryHorizontalDivider()
                        }
                    }
                }
                if (suggestedComponents.halfProducts.isNotEmpty()) {
                    item {
                        SuggestionListHeader(stringResource(R.string.matching_half_products))
                    }
                    itemsIndexed(suggestedComponents.halfProducts) { index, item ->
                        SuggestionItem(
                            headlineText = item.name,
                            painter = painterResource(R.drawable.blender_24),
                        ) { onSelectComponent(item) }
                        if (index < suggestedComponents.halfProducts.size - 1) {
                            FCCSecondaryHorizontalDivider()
                        }
                    }
                }
            } else if (selectedComponent == null) {
                item {
                    if (newComponentName.isBlank()) {
                        EmptySearch()
                    } else {
                        NothingFound(newComponentName)
                    }
                }
            }
        }

        val text = when (selectedComponent) {
            is ProductDomain -> stringResource(R.string.add_product)
            is HalfProductDomain -> stringResource(R.string.add_half_product)
            else -> stringResource(R.string.create_new_product_placeholder, newComponentName)
        }
        AnimatedVisibility(newComponentName.isNotBlank()) {
            FCCOutlinedButton(
                text, modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                enabled = newComponentName.isNotBlank()
            ) {
                onNext()
            }
        }
    }
}

@Composable
private fun SuggestionItem(
    painter: Painter,
    headlineText: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    ListItem(
        modifier = modifier.clickable { onClick() },
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent,
        ),
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
        },
        trailingContent = {
            Icon(
                Icons.AutoMirrored.Sharp.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}

@Composable
private fun SuggestionListHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
private fun EmptySearch(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Sharp.Search,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        )
        Text(
            text = stringResource(R.string.find_an_ingredient),
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = stringResource(R.string.start_typing_to_search_or_create),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun NothingFound(name: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.search_off_24),
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        )
        Text(
            text = stringResource(R.string.no_results_for, name),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
    }
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
                        canonicalPrice = 2.5,
                        tax = 8.0,
                        waste = 5.0,
                        canonicalUnit = MeasurementUnit.KILOGRAM,
                        inputMethod = InputMethod.UNIT,
                        packagePrice = null,
                        packageQuantity = null,
                        packageUnit = null
                    ),
                    ProductDomain(
                        id = 2L,
                        name = "Sugar",
                        canonicalPrice = 3.0,
                        tax = 8.0,
                        waste = 3.0,
                        canonicalUnit = MeasurementUnit.KILOGRAM,
                        inputMethod = InputMethod.UNIT,
                        packagePrice = null,
                        packageQuantity = null,
                        packageUnit = null
                    )
                ),
                halfProducts = listOf(
                    HalfProductDomain(
                        id = 1L,
                        name = "Dough",
                        halfProductUnit = MeasurementUnit.KILOGRAM,
                        products = emptyList()
                    )
                )
            ),
            showSuggestedComponents = true,
            newComponentName = "Fl",
            onNewComponentNameChange = {},
            onSelectComponent = {},
            onNext = {},
            selectedComponent = null,
            modifier = Modifier
        )
    }
}