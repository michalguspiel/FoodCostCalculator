package com.erdees.foodcostcalc.ui.screens.featureRequest

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.ui.composables.InfoBox
import com.erdees.foodcostcalc.ui.composables.ScreenLoadingOverlay
import com.erdees.foodcostcalc.ui.composables.buttons.FCCPrimaryButton
import com.erdees.foodcostcalc.ui.composables.buttons.FCCTopAppBarNavIconButton
import com.erdees.foodcostcalc.ui.composables.dialogs.ErrorDialog
import com.erdees.foodcostcalc.ui.composables.dialogs.FCCDialog
import com.erdees.foodcostcalc.ui.composables.fields.FCCTextField
import com.erdees.foodcostcalc.ui.composables.rows.ButtonRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeatureRequestScreen(
    navController: NavController,
    viewModel: FeatureRequestViewModel = viewModel()
) {
    val screenState by viewModel.screenState.collectAsState()
    val title by viewModel.title.collectAsState()
    val description by viewModel.description.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.feature_request)) },
                navigationIcon = {
                    FCCTopAppBarNavIconButton(navController = navController)
                }
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier.padding(paddingValues)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    stringResource(R.string.submit_feature_request),
                    style = MaterialTheme.typography.titleLarge,
                )

                FCCTextField(
                    value = title,
                    onValueChange = viewModel::updateTitle,
                    title = stringResource(R.string.title),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    )
                )

                FCCTextField(
                    value = description,
                    onValueChange = viewModel::updateDescription,
                    title = stringResource(R.string.description),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        autoCorrectEnabled = true,
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    maxLines = 5,
                    singleLine = false
                )

                InfoBox(
                    title = stringResource(R.string.visibility_of_feature_requests),
                    description = stringResource(R.string.only_approved_requests_info),
                    modifier = Modifier
                )

                Spacer(modifier = Modifier.weight(1f))

                ButtonRow(primaryButton = {
                    FCCPrimaryButton(
                        text = stringResource(R.string.submit),
                        enabled = screenState != FeatureRequestScreenState.LOADING
                    ) {
                        viewModel.submitFeatureRequest(
                            title = title,
                            description = description,
                        )
                    }
                })
            }

            when (screenState) {
                FeatureRequestScreenState.LOADING -> {
                    ScreenLoadingOverlay(Modifier.fillMaxSize())
                }

                FeatureRequestScreenState.SUCCESS -> {
                    FCCDialog(
                        title = stringResource(R.string.success),
                        content = { Text(stringResource(R.string.feature_submitted_successfully)) },
                        onDismiss = {
                            viewModel.resetScreenState()
                            navController.popBackStack()
                        },
                        onPrimaryButtonClick = {
                            viewModel.resetScreenState()
                            navController.popBackStack()
                        },
                        primaryButtonText = stringResource(R.string.okay)
                    )
                }

                FeatureRequestScreenState.ERROR -> {
                    ErrorDialog(onDismiss = viewModel::resetScreenState)
                }

                FeatureRequestScreenState.IDLE -> {
                    /* no-op */
                }
            }
        }
    }
}


enum class FeatureRequestScreenState {
    LOADING, SUCCESS, ERROR, IDLE
}