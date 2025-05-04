package com.erdees.foodcostcalc.ui.composables

import android.icu.util.Currency
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.domain.model.dish.DishDomain
import com.erdees.foodcostcalc.ui.composables.dividers.FCCSecondaryHorizontalDivider
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
    Column(
        modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        (dishDomain.products + dishDomain.halfProducts).forEach {
            IngredientRow(
                modifier = Modifier.padding(bottom = 4.dp),
                description = it.item.name,
                quantity = stringResource(
                    R.string.formatted_quantity,
                    it.formatQuantityForTargetServing(servings),
                    UnitsUtils.getUnitAbbreviation(it.quantityUnit)
                ),
                price = it.formattedTotalPricePerServing(servings, currency),
                style = MaterialTheme.typography.bodyMedium,
                showPrice = showPrices
            )
            FCCSecondaryHorizontalDivider()
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}