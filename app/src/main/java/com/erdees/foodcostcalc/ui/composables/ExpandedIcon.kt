package com.erdees.foodcostcalc.ui.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.erdees.foodcostcalc.R

@Composable
fun ExpandedIcon(isExpanded: Boolean, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        if (isExpanded) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowUp,
                contentDescription = stringResource(
                    id = R.string.collapsed
                )
            )
        } else {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = stringResource(
                    id = R.string.expanded
                )
            )
        }
    }
}

@Preview
@Composable
private fun ExpandedIconPreview() {
    Column {
        ExpandedIcon(isExpanded = true)
        Spacer(modifier = Modifier.size(8.dp))
        ExpandedIcon(isExpanded = false)
    }
}