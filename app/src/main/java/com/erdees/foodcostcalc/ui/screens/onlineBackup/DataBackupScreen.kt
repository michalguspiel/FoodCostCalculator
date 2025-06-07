package com.erdees.foodcostcalc.ui.screens.onlineBackup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.domain.model.FCCUser
import com.erdees.foodcostcalc.domain.model.Operation
import com.erdees.foodcostcalc.domain.model.ScreenState
import com.erdees.foodcostcalc.ui.composables.NavigationListItem
import com.erdees.foodcostcalc.ui.composables.ScreenLoadingOverlay
import com.erdees.foodcostcalc.ui.composables.Section
import com.erdees.foodcostcalc.ui.composables.buttons.FCCTopAppBarNavIconButton
import com.erdees.foodcostcalc.ui.composables.dialogs.ErrorDialog
import com.erdees.foodcostcalc.ui.composables.dialogs.FCCDialog
import com.erdees.foodcostcalc.ui.composables.dividers.FCCSecondaryHorizontalDivider
import com.erdees.foodcostcalc.ui.composables.labels.FieldLabel
import com.erdees.foodcostcalc.ui.navigation.Screen
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import timber.log.Timber


@OptIn(ExperimentalMaterial3Api::class)
@Screen
@Composable
fun DataBackupScreen(navController: NavController, viewModel: OnlineBackupViewModel = viewModel()) {

    val context = LocalContext.current
    val user by viewModel.user.collectAsState()
    val screenState by viewModel.screenState.collectAsState()

    val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .build()
    val googleSignInClient: GoogleSignInClient =
        GoogleSignIn.getClient(context, googleSignInOptions)

    val signInLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val googleSignInAccount = task.getResult(ApiException::class.java)
                viewModel.setUser(
                    FCCUser(
                        googleSignInAccount,
                        googleSignInAccount?.email ?: "",
                        googleSignInAccount?.photoUrl
                    )
                )
                viewModel.checkForGooglePermissions(context, googleSignInAccount)
                viewModel.driveSetUp(context)
            } catch (e: ApiException) {
                Timber.w("signInResult:failed code=" + e.statusCode)
            }
        }

    LaunchedEffect(Unit) {
        val googleSignInAccount = GoogleSignIn.getLastSignedInAccount(context)
        if (googleSignInAccount != null) {
            viewModel.setUser(
                FCCUser(
                    googleSignInAccount,
                    googleSignInAccount.email ?: "",
                    googleSignInAccount.photoUrl
                )
            )
            viewModel.checkForGooglePermissions(context, googleSignInAccount)
            viewModel.driveSetUp(context)
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.data_backup)) },
                navigationIcon = {
                    FCCTopAppBarNavIconButton(navController = navController)
                }
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (user != null) {
                    user?.let { safeUser ->
                        Section(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            tonalElevation = 2.dp
                        ) {
                            safeUser.profilePicUrl?.let {
                                Image(
                                    painter = rememberAsyncImagePainter(it),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .padding(bottom = 8.dp)
                                        .size(96.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Text(text = safeUser.email, style = MaterialTheme.typography.titleLarge)
                        }

                        Spacer(modifier = Modifier.size(8.dp))

                        Section(tonalElevation = 2.dp) {

                            FieldLabel(text = stringResource(R.string.data_backup_save))
                            Text(
                                text = stringResource(R.string.disclaimer_save_db),
                                style = MaterialTheme.typography.bodySmall
                            )
                            NavigationListItem(
                                title = stringResource(R.string.save_database),
                                icon = null
                            ) {
                                viewModel.saveDatabase(context.applicationContext)
                            }

                            FCCSecondaryHorizontalDivider(Modifier.padding(vertical = 4.dp))

                            FieldLabel(text = stringResource(id = R.string.load))
                            Text(
                                text = stringResource(R.string.disclaimer_load_db),
                                style = MaterialTheme.typography.bodySmall
                            )
                            NavigationListItem(
                                title = stringResource(id = R.string.load_database),
                                icon = null
                            ) {
                                viewModel.loadDatabase(context.applicationContext)
                            }

                            FCCSecondaryHorizontalDivider(Modifier.padding(vertical = 4.dp))

                            NavigationListItem(
                                title = stringResource(id = R.string.sign_out),
                                icon = null
                            ) {
                                viewModel.signOut(googleSignInClient)
                            }
                        }
                    }
                } else {
                    Section(tonalElevation = 2.dp) {
                        FieldLabel(text = stringResource(id = R.string.sign_field_title))
                        Text(
                            text = stringResource(id = R.string.disclaimer_sign_in),
                            style = MaterialTheme.typography.bodySmall
                        )
                        NavigationListItem(
                            title = stringResource(id = R.string.sign_in),
                            icon = null
                        ) {
                            signInLauncher.launch(googleSignInClient.signInIntent)
                        }
                    }
                }
            }
        }
    )

    when (screenState) {
        is ScreenState.Error -> {
            val error = (screenState as ScreenState.Error).error
            ErrorDialog(
                title = stringResource(id = R.string.error),
                onDismiss = viewModel::resetScreenState,
                content = error.message ?: stringResource(id = R.string.something_went_wrong),
            )
        }

        ScreenState.Idle -> {

        }

        is ScreenState.Interaction -> {}

        is ScreenState.Loading<*> -> {
            val loadingText = when ((screenState as ScreenState.Loading<*>).data) {
                Operation.DB_LOAD -> stringResource(id = R.string.db_loading_in_progress)
                Operation.DB_SAVE -> stringResource(id = R.string.db_saving_in_progress)
                else -> stringResource(id = R.string.operation_in_progress)
            }
            ScreenLoadingOverlay(loadingText = loadingText)
        }

        is ScreenState.Success<*> -> {
            val successText = when ((screenState as ScreenState.Success<*>).data) {
                Operation.DB_LOAD -> stringResource(id = R.string.db_loaded_successfully)
                Operation.DB_SAVE -> stringResource(id = R.string.db_saved_successfully)
                else -> stringResource(id = R.string.operation_successful)
            }
            FCCDialog(
                title = stringResource(id = R.string.success),
                primaryButtonText = stringResource(id = R.string.okay),
                onDismiss = viewModel::resetScreenState,
                onPrimaryButtonClick = viewModel::resetScreenState
            ) {
                Text(successText)
            }
        }
    }
}