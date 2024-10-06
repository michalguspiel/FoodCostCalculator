package com.erdees.foodcostcalc.ui.screens.onlineBackup

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.erdees.foodcostcalc.data.AppRoomDataBase
import com.erdees.foodcostcalc.data.repository.AnalyticsRepository
import com.erdees.foodcostcalc.domain.model.FCCUser
import com.erdees.foodcostcalc.domain.model.Operation
import com.erdees.foodcostcalc.domain.model.ScreenState
import com.erdees.foodcostcalc.domain.model.errors.DownloadFailure
import com.erdees.foodcostcalc.domain.model.errors.DrivePermissionFailure
import com.erdees.foodcostcalc.domain.model.errors.DriveQueryFailure
import com.erdees.foodcostcalc.domain.model.errors.DriveSetupFailure
import com.erdees.foodcostcalc.domain.model.errors.FailedToSignOut
import com.erdees.foodcostcalc.domain.model.errors.NoDatabaseFileError
import com.erdees.foodcostcalc.domain.model.errors.SavingDatabaseFailure
import com.erdees.foodcostcalc.ui.MyApplication
import com.erdees.foodcostcalc.utils.Constants
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.FileList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.Collections

class OnlineBackupViewModel : ViewModel(), KoinComponent {

    private var googleDriveService: Drive? = null
    private var driveServiceHelper: DriveServiceHelper? = null
    private val analyticsRepository: AnalyticsRepository by inject()

    private var _screenState: MutableStateFlow<ScreenState> = MutableStateFlow(ScreenState.Idle)
    val screenState: StateFlow<ScreenState> = _screenState

    private var _user: MutableStateFlow<FCCUser?> = MutableStateFlow(null)
    val user: StateFlow<FCCUser?> = _user

    fun setUser(user: FCCUser) {
        _user.value = user
    }

    fun resetScreenState() {
        _screenState.value = ScreenState.Idle
    }

    private fun accountDoesNotHaveGooglePermissions(): Boolean {
        val account = user.value?.googleSignInAccount ?: return false
        return !GoogleSignIn.hasPermissions(
            account,
            Scope(Scopes.DRIVE_FILE),
            Scope(Scopes.EMAIL)
        )
    }

    fun checkForGooglePermissions(context: Context, account: GoogleSignInAccount?) {
        if (accountDoesNotHaveGooglePermissions()) {
            GoogleSignIn.requestPermissions(
                context as Activity,
                RC_SIGN_IN,
                account,
                Scope(Scopes.DRIVE_FILE),
                Scope(Scopes.EMAIL)
            )
        }
    }

    fun loadDatabase(context: Context) {
        analyticsRepository.logEvent(Constants.Analytics.LOAD_DATABASE, null)
        if (accountDoesNotHaveGooglePermissions()) {
            handleFailure(DrivePermissionFailure, null)
            return
        }
        viewModelScope.launch {
            _screenState.value = ScreenState.Loading(Operation.DB_LOAD)
            withContext(Dispatchers.IO) {
                loadFileFromDrive(context)
            }
        }
    }

    fun saveDatabase(context: Context) {
        analyticsRepository.logEvent(Constants.Analytics.SAVE_DATABASE, null)
        if (accountDoesNotHaveGooglePermissions()) {
            handleFailure(DrivePermissionFailure, null)
            return
        }
        viewModelScope.launch {
            _screenState.value = ScreenState.Loading(Operation.DB_SAVE)
            withContext(Dispatchers.IO) {
                upsertDatabaseInDrive(context)
            }
        }
    }

    fun signOut(googleSignInClient: GoogleSignInClient) {
        _screenState.value = ScreenState.Loading()
        googleSignInClient.signOut()
            .addOnSuccessListener {
                resetScreenState()
                _user.value = null
            }
            .addOnFailureListener { handleFailure(FailedToSignOut, null) }
    }

    fun driveSetUp(applicationContext: Context) {
        val credential = GoogleAccountCredential.usingOAuth2(
            applicationContext, Collections.singleton(Scopes.DRIVE_FILE)
        )
        credential.selectedAccount = user.value?.googleSignInAccount?.account
        googleDriveService = Drive.Builder(
            AndroidHttp.newCompatibleTransport(),
            GsonFactory(),
            credential
        ).setApplicationName("Food Cost Calculator").build().also {
            driveServiceHelper = DriveServiceHelper(it)
        }
    }

    private suspend fun loadFileFromDrive(context: Context) {
        val file = File(context.getExternalFilesDir(null), AppRoomDataBase.NAME)
        val driveServiceHelper = driveServiceHelper ?: run {
            handleFailure(DriveSetupFailure, null)
            return
        }
        val result = driveServiceHelper.queryFiles()

        val files = result.getOrNull()
        if (files == null) {
            handleFailure(NoDatabaseFileError, null)
            return
        }

        driveServiceHelper.onQueryFilesSuccess(files, context, file)
    }

    private suspend fun DriveServiceHelper.onQueryFilesSuccess(
        fileList: FileList,
        context: Context,
        file: File
    ) {
        if (fileList.files.isEmpty()) {
            handleFailure(NoDatabaseFileError, null)
            return
        }
        val database = fileList.files.first()
        Log.i(TAG, "onQueryFilesSuccess: ${database.id}, ${database.name}, ${database.size}")
        val result = downloadFile(file, database.id)
        when {
            result.isSuccess -> swapDatabaseFiles(file, context)
            result.isFailure -> handleFailure(DownloadFailure, null)
        }
    }

    /**
     * Swaps the current database file with a new one and recreates the Room database instance.
     *
     * This method performs the following steps:
     * 1. Logs the start of the database file swap process.
     * 2. Defines a RoomDatabase.Callback to handle the onOpen event, which logs the successful opening
     *    of the database, restarts Koin, and updates the screen state to success.
     * 3. Closes the current Room database instance.
     * 4. Deletes the existing database file.
     * 5. Copies the new database file to the appropriate location.
     * 6. Recreates the Room database instance from the new file and attaches the callback.
     *
     * @param file The new database file to be used.
     * @param applicationContext The application context used to access the database and restart Koin.
     */
    private fun swapDatabaseFiles(file: File, applicationContext: Context) {
        Log.i(TAG, "Swapping database files $file, ${file.length()}")
        val roomDatabaseCallback = object : RoomDatabase.Callback() {
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                Log.i("AppRoomDataBase", "Database opened successfully.")
                handleSuccess(applicationContext, Operation.DB_LOAD)
            }
        }
        AppRoomDataBase.getDatabase(applicationContext).close()
        val dbFile = getDatabaseFile(applicationContext)
        dbFile.delete()
        copyFile(file.absolutePath, dbFile.absolutePath)
        AppRoomDataBase.recreateDatabaseFromFile(
            applicationContext,
            file,
            roomDatabaseCallback
        )
    }

    private suspend fun upsertDatabaseInDrive(context: Context) {
        val driveServiceHelper = driveServiceHelper ?: run {
            handleFailure(DriveSetupFailure, null)
            return
        }
        val result = driveServiceHelper.queryFiles()
        if (result.isFailure) {
            handleFailure(DriveQueryFailure, null)
            return
        }

        val fileList = result.getOrNull()
        Log.i(TAG, "fileList: ${fileList?.files?.size}")

        // Close database before uploading, so that the checkpoint is performed
        // If we fail after this point we need to destroy db instance and restart koin
        AppRoomDataBase.getDatabase(context).close()

        if (fileList?.files.isNullOrEmpty() || fileList == null) {
            driveServiceHelper.saveDataBaseInDrive(context)
        } else {
            driveServiceHelper.updateDatabaseInDrive(fileList.files.first(), context)
        }
    }

    /**
     * Updates the database file in Google Drive by deleting the existing file and uploading a new one.
     *
     * This method performs the following steps:
     * 1. Deletes the existing database file in Google Drive.
     * 2. If the deletion is successful, it calls [saveDataBaseInDrive] to upload the new database file.
     * 3. If the deletion fails, it handles the failure by logging the error and updating the screen state.
     *
     * @param file The existing database file in Google Drive to be deleted.
     * @param context The context used to access the local database file for uploading.
     */
    private suspend fun DriveServiceHelper.updateDatabaseInDrive(
        file: com.google.api.services.drive.model.File,
        context: Context
    ) {
        Log.i(TAG, "Updating database in Google Drive")
        val result = deleteFolderFile(file.id)
        when {
            result.isSuccess -> saveDataBaseInDrive(context)
            result.isFailure -> handleFailure(SavingDatabaseFailure, context)
        }
    }

    private suspend fun DriveServiceHelper.saveDataBaseInDrive(context: Context) {
        Log.i(TAG, "Saving database in Google Drive")
        val result = uploadFile(getDatabaseFile(context), null, null)
        when {
            result.isSuccess -> {
                result.getOrNull()?.let {
                    Log.i(
                        TAG,
                        "onSuccess of save database ${it.id}, ${it.name}, ${it.size}, ${it.createdTime}"
                    )
                }
                handleSuccess(context, Operation.DB_SAVE)
            }

            result.isFailure -> handleFailure(SavingDatabaseFailure, context)
        }
    }

    /**
     * Handles success.
     * Destroys database, so that the new instance will be created.
     * Restarts Koin, so that dependencies will create new instance of DB.
     * Updates screen state to success.
     * */
    private fun handleSuccess(context: Context, operation: Operation) {
        analyticsRepository.logEvent(Constants.Analytics.DATABASE_OPERATION_SUCCESS, null)
        AppRoomDataBase.destroyInstance()
        (context as MyApplication).restartKoin()
        _screenState.value = ScreenState.Success(operation)
    }

    /**
     * Logs failure, changes the screen state to error, and if context is not null, destroys database and restarts Koin.
     * */
    private fun handleFailure(error: Error, context: Context?) {
        val bundle = Bundle()
        bundle.putString(
            Constants.Analytics.DATABASE_OPERATION_ERROR,
            error.message ?: error.javaClass.simpleName
        )
        analyticsRepository.logEvent(Constants.Analytics.DATABASE_OPERATION_FAILURE, bundle)
        context?.let {
            AppRoomDataBase.destroyInstance()
            (context as MyApplication).restartKoin()
        }
        _screenState.value = ScreenState.Error(error)
    }

    private fun getDatabaseFile(context: Context): File {
        return context.getDatabasePath(AppRoomDataBase.NAME).absoluteFile.also {
            Log.i(TAG, "getDatabaseFile file path: $it")
        }
    }

    private fun copyFile(inputPath: String, outputPath: String) {
        Log.i(TAG, "Copying file from $inputPath to $outputPath")
        val inputStream: InputStream?
        val out: OutputStream?
        try {
            inputStream = FileInputStream(inputPath)
            out = FileOutputStream(outputPath)
            val buffer = ByteArray(1024)
            var read: Int
            while (inputStream.read(buffer).also { read = it } != -1) {
                out.write(buffer, 0, read)
            }
            inputStream.close()
            // write the output file (You have now copied the file)
            out.flush()
            out.close()
        } catch (e: FileNotFoundException) {
            Log.e(TAG, e.message ?: "Unknown error")
        } catch (e: Exception) {
            Log.e(TAG, e.message ?: "Unknown error")
        }
    }

    companion object {
        private const val RC_SIGN_IN = 213
        private const val TAG = "OnlineBackupViewModel"
    }
}