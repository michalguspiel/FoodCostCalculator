package com.erdees.foodcostcalc.ui.composables.emptylist

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.erdees.foodcostcalc.ui.composables.buttons.FCCPrimaryButton
import com.erdees.foodcostcalc.ui.navigation.FCCScreen
import com.erdees.foodcostcalc.ui.theme.FCCTheme

@Composable
fun EmptyListContent(
    screen: FCCScreen, modifier: Modifier = Modifier, ctaClick: () -> Unit = {}
) {

    val scrollState = rememberScrollState()
    val config = screen.emptyListConfig()

    Column(
        modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {

        Spacer(Modifier.size(32.dp))
        Text(
            text = stringResource(config.titleRes),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight =
                    FontWeight.Medium
            )
        )

        Text(
            text = stringResource(config.descriptionRes),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )

        Image(
            painter = painterResource(config.iconRes),
            contentDescription = stringResource(config.iconContentDescriptionRes),
            modifier = Modifier.size(64.dp),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)

        )

        Spacer(Modifier.weight(1f))

        FCCPrimaryButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            text = stringResource(config.buttonTextRes),
            enabled = true
        ) {
            ctaClick()
        }
    }
}

@Preview(name = "Empty List - Products", showBackground = true)
@Composable
private fun PreviewEmptyListContentProducts() {
    FCCTheme {
        EmptyListContent(screen = FCCScreen.Products)
    }
}

@Preview(name = "Empty List - HalfProducts", showBackground = true)
@Composable
private fun PreviewEmptyListContentHalfProducts() {
    FCCTheme {
        EmptyListContent(screen = FCCScreen.HalfProducts)
    }
}

@Preview(name = "Empty List - Dishes", showBackground = true)
@Composable
private fun PreviewEmptyListContentDishes() {
    FCCTheme {
        EmptyListContent(screen = FCCScreen.Dishes())
    }
}