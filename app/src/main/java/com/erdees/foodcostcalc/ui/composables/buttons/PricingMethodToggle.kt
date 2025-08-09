package com.erdees.foodcostcalc.ui.composables.buttons

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Generic reusable two-option segmented button toggle component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TwoOptionToggle(
    option1Text: String,
    option2Text: String,
    selectedIndex: Int,
    onSelectionChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedIndexState by remember(selectedIndex) {
        mutableIntStateOf(selectedIndex)
    }

    SingleChoiceSegmentedButtonRow(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 200.dp)
    ) {
        SegmentedButton(
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            onClick = {
                if (selectedIndexState != 0) {
                    selectedIndexState = 0
                    onSelectionChange(0)
                }
            },
            selected = selectedIndexState == 0
        ) {
            Text(option1Text)
        }
        SegmentedButton(
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            onClick = {
                if (selectedIndexState != 1) {
                    selectedIndexState = 1
                    onSelectionChange(1)
                }
            },
            selected = selectedIndexState == 1
        ) {
            Text(option2Text)
        }
    }
}
