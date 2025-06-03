package com.erdees.foodcostcalc.ui.screens.featureRequestList

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.data.model.remote.FeatureRequestStatus
import com.erdees.foodcostcalc.domain.model.FeatureRequestDomain
import com.erdees.foodcostcalc.ui.composables.ScreenLoadingOverlay
import com.erdees.foodcostcalc.ui.composables.buttons.FCCTopAppBarNavIconButton
import com.erdees.foodcostcalc.ui.composables.emptylist.EmptyListContent
import com.erdees.foodcostcalc.ui.navigation.FCCScreen
import com.erdees.foodcostcalc.ui.theme.FCCTheme
import com.erdees.foodcostcalc.utils.Formatter
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date

@Composable
fun FeatureRequestListScreen(
    navController: NavController,
    viewModel: FeatureRequestListViewModel = viewModel()
) {

    val featureRequests by viewModel.featureRequestList.collectAsState()
    FeatureRequestListContent(
        featureRequests = featureRequests,
        navController = navController,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeatureRequestListContent(
    navController: NavController,
    featureRequests: List<FeatureRequestDomain>?,
) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.feature_requests)) },
                navigationIcon = {
                    FCCTopAppBarNavIconButton(navController = navController)
                }
            )
        },
    ) { paddingValues ->
        featureRequests?.let {
            if (featureRequests.isEmpty()) {
                EmptyListContent(FCCScreen.FeatureRequestList) {
                    navController.navigate(FCCScreen.FeatureRequest)
                }
            } else {
                LazyColumn(modifier = Modifier.padding(paddingValues)) {
                    items(items = featureRequests, key = { it.id }) { featureRequest ->
                        FeatureRequestCard(Modifier.padding(8.dp), featureRequest)
                    }
                }
            }
        } ?: ScreenLoadingOverlay()


    }
}

@Composable
private fun FeatureRequestCard(
    modifier: Modifier = Modifier,
    featureRequest: FeatureRequestDomain
) {
    val status = when (featureRequest.status) {
        FeatureRequestStatus.IN_PROGRESS -> stringResource(id = R.string.feature_request_status_in_progress)
        FeatureRequestStatus.APPROVED -> stringResource(id = R.string.feature_request_status_approved)
        FeatureRequestStatus.PENDING -> stringResource(id = R.string.feature_request_status_pending)
        FeatureRequestStatus.COMPLETED -> stringResource(id = R.string.feature_request_status_completed)
        FeatureRequestStatus.UNKNOWN -> stringResource(id = R.string.feature_request_status_unknown)
    }

    Card(modifier = modifier) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(text = featureRequest.title, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.size(6.dp))
            Text(text = featureRequest.description, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.size(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = status,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = stringResource(
                        R.string.submitted_on_date,
                        featureRequest.formattedTimeStamp
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun FeatureRequestCardPreview() {
    FCCTheme {
        FeatureRequestCard(
            featureRequest =
                FeatureRequestDomain(
                    id = "1",
                    title = "Dark Mode Support",
                    status = FeatureRequestStatus.IN_PROGRESS,
                    description = "Implement a dark theme for the application to reduce eye strain in low light conditions.",
                    formattedTimeStamp = "19.02.2025",
                    upVotes = 125
                )
        )
    }
}

@Preview(showBackground = true, name = "Feature Request List Content")
@Composable
private fun FeatureRequestListPreview() {
    // Use a Theme if your FeatureRequestListContent relies on it for styling
    FCCTheme { // Assuming you have a theme like this
        FeatureRequestListContent(
            featureRequests = listOf(
                FeatureRequestDomain(
                    id = "1",
                    title = "Dark Mode Support",
                    status = FeatureRequestStatus.IN_PROGRESS,
                    description = "Implement a dark theme for the application to reduce eye strain in low light conditions.",
                    formattedTimeStamp = "19/02/2025",
                    upVotes = 125
                ),
                FeatureRequestDomain(
                    id = "2",
                    title = "Export Data to CSV",
                    status = FeatureRequestStatus.APPROVED,
                    description = "Allow users to export their cost calculation data into a CSV file for external analysis.",
                    formattedTimeStamp = "19/02/2025",
                    upVotes = 78
                ),
                FeatureRequestDomain(
                    id = "3",
                    status = FeatureRequestStatus.APPROVED,
                    title = "Multi-language Support",
                    description = "Add support for other languages like Spanish and French.",
                    formattedTimeStamp = Formatter.formatTimeStamp(
                        Date.from(
                            Instant.now().minus(12, ChronoUnit.DAYS)
                        )
                    ),
                    upVotes = 210
                ),
                FeatureRequestDomain(
                    id = "4",
                    status = FeatureRequestStatus.APPROVED,
                    title = "Integration with Cloud Storage",
                    description = "Allow users to back up and sync their data with services like Google Drive or Dropbox.",
                    formattedTimeStamp = Formatter.formatTimeStamp(
                        Date.from(
                            Instant.now().minus(7, ChronoUnit.DAYS)
                        )
                    ),
                    upVotes = 45
                ),
                FeatureRequestDomain(
                    // Example of unapproved request
                    id = "5",
                    title = "Recipe Sharing Feature",
                    description = "Users should be able to share their recipes with other users of the app.",
                    formattedTimeStamp = Formatter.formatTimeStamp(
                        Date.from(
                            Instant.now().minus(1, ChronoUnit.DAYS)
                        )
                    ),
                )
            ),
            navController = rememberNavController()
        )
    }
}

@Preview(showBackground = true, name = "Feature Request List Content - Empty")
@Composable
private fun FeatureRequestListPreviewEmpty() {
    FCCTheme {
        FeatureRequestListContent(
            featureRequests = emptyList(),
            navController = rememberNavController()
        )
    }
}

@Preview(showBackground = true, name = "Feature Request List Content - Loading (Null)")
@Composable
private fun FeatureRequestListPreviewLoading() {
    FCCTheme {
        FeatureRequestListContent(
            featureRequests = null, // Simulate loading state if your ViewModel initializes with null
            navController = rememberNavController()
        )
    }
}