package com.erdees.foodcostcalc.ui.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.KeyboardArrowRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.erdees.foodcostcalc.R

@Composable
fun NavigationListItem(
    title: String,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)?,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .height(48.dp)
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            icon?.let {
                it()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(text = title)
        }
        Icon(
            modifier = Modifier.size(24.dp),
            imageVector = Icons.AutoMirrored.Sharp.KeyboardArrowRight,
            contentDescription = stringResource(id = R.string.navigate_to, title)
        )
    }
}

@Preview
@Composable
private fun PreviewNavigationListItem() {
    NavigationListItem(
        title = "Sample Title",
        icon = { Icon(imageVector = Icons.Default.Home, contentDescription = null) },
        onClick = {}
    )
}