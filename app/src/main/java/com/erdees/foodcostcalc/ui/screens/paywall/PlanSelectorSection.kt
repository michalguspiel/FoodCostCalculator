package com.erdees.foodcostcalc.ui.screens.paywall

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SingleChoiceSegmentedButtonRowScope
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.domain.model.premiumSubscription.Plan
import com.erdees.foodcostcalc.ext.conditionally
import com.erdees.foodcostcalc.ui.theme.FCCTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanSelectorSection(
    monthlyPlan: Plan,
    yearlyPlan: Plan,
    selectedPlan: Plan?,
    onPlanSelected: (Plan) -> Unit,
) {
    val savePercent = calculateSavePercent(monthlyPlan.priceInMicros, yearlyPlan.priceInMicros)
    val savePercentString = stringResource(R.string.save_x_percent, savePercent.toString())
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            PlanButton(
                plan = monthlyPlan,
                selectedPlan = selectedPlan,
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                promotePlan = false,
                onPlanSelected = onPlanSelected,
                savePercentString = savePercentString,
            )
            PlanButton(
                plan = yearlyPlan,
                selectedPlan = selectedPlan,
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                promotePlan = true,
                onPlanSelected = onPlanSelected,
                savePercentString = savePercentString,
            )
        }
    }
}

@Composable
private fun SingleChoiceSegmentedButtonRowScope.PlanButton(
    plan: Plan,
    selectedPlan: Plan?,
    shape: Shape,
    promotePlan: Boolean,
    modifier: Modifier = Modifier,
    savePercentString: String,
    onPlanSelected: (Plan) -> Unit,
) {
    SegmentedButton(
        modifier = modifier
            .weight(1f)
            .fillMaxHeight(),
        shape = shape,
        onClick = { onPlanSelected(plan) },
        selected = selectedPlan?.id == plan.id,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = stringResource(R.string.best_value),
                style = MaterialTheme.typography.labelMedium.copy(
                    color = MaterialTheme.colorScheme.onTertiary
                ),
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(50)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                    .conditionally(!promotePlan) {
                        Modifier.alpha(0.0f)
                    },
            )

            Text(
                text = stringResource(R.string.yearly),
                style = MaterialTheme.typography.labelLarge
            )

            Text(
                text = plan.formattedPrice,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                modifier = Modifier.conditionally(!promotePlan) {
                    Modifier.alpha(0.0f)
                },
                text = savePercentString,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontStyle = FontStyle.Italic
                )
            )
        }
    }
}

private fun calculateSavePercent(monthlyPriceInMicros: Long, yearlyPriceInMicros: Long): Int {
    return if (monthlyPriceInMicros > 0) {
        ((1 - (yearlyPriceInMicros / 12f) / monthlyPriceInMicros) * 100).toInt()
    } else {
        0
    }
}

@PreviewLightDark
@Composable
private fun PlanSelectorSectionPreview() {
    val mockMonthlyPlan = Plan(
        id = "monthly",
        offerIdToken = "monthly_token",
        billingPeriod = "P1M",
        formattedPrice = "€4.99",
        currencyCode = "EUR",
        priceInMicros = 4990000
    )

    val mockYearlyPlan = Plan(
        id = "yearly",
        offerIdToken = "yearly_token",
        billingPeriod = "P1Y",
        formattedPrice = "€39.99",
        currencyCode = "EUR",
        priceInMicros = 39990000
    )

    FCCTheme {
        Surface {
            PlanSelectorSection(mockMonthlyPlan, mockYearlyPlan, selectedPlan = mockYearlyPlan) { }
        }
    }
}

@PreviewLightDark
@Composable
private fun PlanSelectorSectionMonthlySelectedPreview() {
    val mockMonthlyPlan = Plan(
        id = "monthly",
        offerIdToken = "monthly_token",
        billingPeriod = "P1M",
        formattedPrice = "€4.99",
        currencyCode = "EUR",
        priceInMicros = 4990000
    )

    val mockYearlyPlan = Plan(
        id = "yearly",
        offerIdToken = "yearly_token",
        billingPeriod = "P1Y",
        formattedPrice = "€39.99",
        currencyCode = "EUR",
        priceInMicros = 39990000
    )

    FCCTheme {
        Surface {
            PlanSelectorSection(mockMonthlyPlan, mockYearlyPlan, selectedPlan = mockMonthlyPlan) { }
        }
    }
}