package com.erdees.foodcostcalc.ui.composables

import android.icu.util.Currency
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.domain.model.ItemUsageEntry
import com.erdees.foodcostcalc.domain.model.dish.DishDomain
import com.erdees.foodcostcalc.ui.composables.dividers.FCCSecondaryHorizontalDivider
import com.erdees.foodcostcalc.ui.composables.dividers.FCCThickSecondaryHorizontalDivider
import com.erdees.foodcostcalc.ui.composables.rows.IngredientRow
import com.erdees.foodcostcalc.utils.UnitsUtils

@Composable
fun Ingredients(
    dishDomain: DishDomain,
    servings: Double,
    currency: Currency?,
    modifier: Modifier = Modifier,
    showPrices: Boolean = true
) {
    Ingredients(dishDomain.products, dishDomain.halfProducts, servings, currency, modifier, showPrices)
}

@Composable
fun Ingredients(
    products: List<ItemUsageEntry>,
    halfProducts: List<ItemUsageEntry>,
    servings: Double,
    currency: Currency?,
    modifier: Modifier = Modifier,
    showPrices: Boolean = true,
    spacious: Boolean = false
) {
    Column(
        modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        (products + halfProducts).forEachIndexed { index, itemUsageEntry ->
            val needsExtraSpace = index != 0 && spacious
            IngredientRow(
                modifier = Modifier.padding(
                    bottom = if (spacious) 8.dp else 4.dp,
                    top = if (needsExtraSpace) 8.dp else 0.dp
                ),
                description = itemUsageEntry.item.name,
                quantity = stringResource(
                    R.string.formatted_quantity,
                    itemUsageEntry.formatQuantityForTargetServing(servings),
                    UnitsUtils.getUnitAbbreviation(itemUsageEntry.quantityUnit)
                ),
                price = itemUsageEntry.formattedTotalPricePerServing(servings, currency),
                showPrice = showPrices
            )
            if (spacious) FCCThickSecondaryHorizontalDivider()
            else FCCSecondaryHorizontalDivider()
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}