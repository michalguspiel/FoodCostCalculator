package com.erdees.foodcostcalc.ui.screens.onlineBackup

import android.util.Log
import com.google.api.client.http.FileContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
import java.util.Collections

class DriveServiceHelper(driveService: Drive) {
    private val mDriveService: Drive = driveService

    suspend fun downloadFile(targetFile: java.io.File?, fileId: String?): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                targetFile?.let { file ->
                    FileOutputStream(file).use { outputStream ->
                        mDriveService.files().get(fileId).executeMediaAndDownloadTo(outputStream)
                    }
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Error downloading file: ${e.message}")
                Result.failure(e)
            }
        }
    }

    suspend fun deleteFolderFile(fileId: String?): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                mDriveService.files().delete(fileId).execute()
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting file: ${e.message}")
                Result.failure(e)
            }
        }
    }

    /**
     * Returns a [FileList] containing all the visible files in the user's My Drive.
     *
     *
     * The returned list will only contain files visible to this app, i.e. those which were
     * created by this app. To perform operations on files not created by the app, the project must
     * request Drive Full Scope in the [Google
     * Developer's Console](https://play.google.com/apps/publish) and be submitted to Google for verification.
     */
    suspend fun queryFiles(): Result<FileList> {
        return withContext(Dispatchers.IO) {
            try {
                val fileList = mDriveService.files().list().setSpaces("drive").execute().also {
                    Log.i(TAG, "queryFiles: $it")
                }
                Result.success(fileList)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun uploadFile(
        localFile: java.io.File,
        mimeType: String?,
        folderId: String?
    ): Result<GoogleDriveFileHolder?> {
        Log.i(TAG, "uploadFile: $localFile")
        return withContext(Dispatchers.IO) {
            try {
                val root: List<String> = if (folderId == null) {
                    Collections.singletonList("root")
                } else {
                    Collections.singletonList(folderId)
                }
                val metadata = File()
                    .setParents(root)
                    .setMimeType(mimeType)
                    .setName(localFile.name)
                val fileContent = FileContent(mimeType, localFile)
                val fileMeta = mDriveService.files().create(
                    metadata,
                    fileContent
                ).execute()
                val googleDriveFileHolder = GoogleDriveFileHolder()
                googleDriveFileHolder.id = fileMeta.id
                googleDriveFileHolder.name = fileMeta.name
                Result.success(googleDriveFileHolder)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    companion object {
        private const val TAG = "DriveServiceHelper"
    }
}