package com.erdees.foodcostcalc.ui.screens.products

import android.icu.util.Currency
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.erdees.foodcostcalc.BuildConfig
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.domain.model.Ad
import com.erdees.foodcostcalc.domain.model.AdItem
import com.erdees.foodcostcalc.domain.model.product.ProductDomain
import com.erdees.foodcostcalc.ui.composables.Ad
import com.erdees.foodcostcalc.ui.composables.ScreenLoadingOverlay
import com.erdees.foodcostcalc.ui.composables.animations.SearchFieldTransition
import com.erdees.foodcostcalc.ui.composables.buttons.FCCAnimatedFAB
import com.erdees.foodcostcalc.ui.composables.buttons.FCCPrimaryButton
import com.erdees.foodcostcalc.ui.composables.dividers.FCCPrimaryHorizontalDivider
import com.erdees.foodcostcalc.ui.composables.emptylist.EmptyListContent
import com.erdees.foodcostcalc.ui.composables.fields.SearchField
import com.erdees.foodcostcalc.ui.composables.rememberNestedScrollConnection
import com.erdees.foodcostcalc.ui.composables.rows.ButtonRow
import com.erdees.foodcostcalc.ui.composables.rows.PriceRow
import com.erdees.foodcostcalc.ui.navigation.FCCScreen
import com.erdees.foodcostcalc.ui.navigation.Screen
import com.erdees.foodcostcalc.ui.theme.FCCTheme
import com.erdees.foodcostcalc.utils.Constants
import com.erdees.foodcostcalc.utils.Utils.formatPrice
import java.util.Locale

@Composable
@Screen
fun ProductsScreen(
    navController: NavController,
    viewModel: ProductsFragmentViewModel = viewModel()
) {
    val searchKey by viewModel.searchKey.collectAsState()
    val listItems by viewModel.filteredProductsInjectedWithAds.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val isEmptyListContentVisible by viewModel.isEmptyListContentVisible.collectAsState()
    val isVisible = rememberSaveable { mutableStateOf(true) }
    val nestedScrollConnection = rememberNestedScrollConnection { isVisible.value = it }

    Scaffold(
        floatingActionButton = {
            if (!isEmptyListContentVisible) {
                FCCAnimatedFAB(
                    isVisible = isVisible.value,
                    contentDescription = stringResource(id = R.string.content_description_create_product)
                ) {
                    navController.navigate(FCCScreen.CreateProduct)
                }
            }
        }
    ) { paddingValues ->
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier
                .padding(paddingValues)
        ) {
            listItems?.let { listItems ->
                if (isEmptyListContentVisible) {
                    EmptyListContent(screen = FCCScreen.Products) {
                        navController.navigate(FCCScreen.CreateProduct)
                    }
                } else {
                    ProductsScreenContent(
                        nestedScrollConnection,
                        listItems,
                        currency,
                        navController,
                        isVisible.value,
                        searchKey,
                        viewModel::onAdFailedToLoad,
                        viewModel::updateSearchKey
                    )
                }
            } ?: ScreenLoadingOverlay(Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun ProductsScreenContent(
    nestedScrollConnection: NestedScrollConnection,
    listItems: List<AdItem>,
    currency: Currency?,
    navController: NavController,
    isVisible: Boolean,
    searchKey: String,
    onAdFailedToLoad: () -> Unit,
    updateSearchKey: (String) -> Unit
) {
    Box(
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            Modifier
                .nestedScroll(nestedScrollConnection)
                .padding(horizontal = 8.dp),
            contentPadding = PaddingValues(top = (36 + 8 + 8).dp)
        ) {
            items(listItems) { item ->
                when (item) {
                    is Ad -> {
                        Ad(
                            modifier = Modifier.padding(vertical = 8.dp),
                            adUnitId = if (BuildConfig.DEBUG) Constants.Ads.ADMOB_TEST_AD_UNIT_ID
                            else Constants.Ads.ADMOB_PRODUCTS_AD_UNIT_ID,
                            onAdFailedToLoad = { onAdFailedToLoad() }
                        )
                    }

                    is ProductDomain -> {
                        ProductItem(
                            productDomain = item,
                            currency = currency,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            navController.navigate(FCCScreen.EditProduct(item.id))
                        }
                    }
                }
            }
        }
        SearchFieldTransition(isVisible = isVisible) {
            SearchField(
                modifier = Modifier,
                value = searchKey,
                onValueChange = { updateSearchKey(it) }
            )
        }
    }
}

@Composable
fun ProductItem(
    productDomain: ProductDomain,
    currency: Currency?,
    modifier: Modifier = Modifier,
    onEditClick: () -> Unit = {}
) {
    Card(modifier.fillMaxWidth(), content = {
        Column(Modifier.padding(vertical = 8.dp, horizontal = 12.dp)) {
            Text(text = productDomain.name, style = MaterialTheme.typography.headlineSmall)

            Spacer(modifier = Modifier.height(8.dp))

            PriceRow(
                description = stringResource(id = R.string.netto_price, productDomain.unit),
                price = formatPrice(productDomain.pricePerUnit, currency)
            )
            Spacer(modifier = Modifier.height(4.dp))
            PriceRow(
                description = stringResource(id = R.string.total_price, productDomain.unit),
                price = formatPrice(productDomain.priceAfterWasteAndTax, currency),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
            )

            Spacer(modifier = Modifier.height(8.dp))

            FCCPrimaryHorizontalDivider()

            Spacer(modifier = Modifier.height(12.dp))
            ButtonRow(applyDefaultPadding = false, primaryButton = {
                FCCPrimaryButton(text = stringResource(id = R.string.edit)) {
                    onEditClick()
                }
            })
        }
    })
}

@Preview
@Composable
private fun ProductItemPreview() {
    FCCTheme {
        ProductItem(
            productDomain = ProductDomain(
                id = 1,
                name = "Product",
                pricePerUnit = 12.23,
                tax = 0.23,
                waste = 0.1,
                unit = "kg"
            ),
            currency = Currency.getInstance(Locale.getDefault())
        )
    }
}