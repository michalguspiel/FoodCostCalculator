package com.erdees.foodcostcalc.drive

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.annotation.Nullable
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.tasks.Tasks.call
import com.google.api.client.http.FileContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import java.io.*
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors


class DriveServiceHelper(driveService: Drive) {
    private val mExecutor: Executor = Executors.newSingleThreadExecutor()
    private val mDriveService: Drive = driveService


    fun downloadFile(targetFile: java.io.File?, fileId: String?): Task<Void?>? {
        return call(mExecutor) {
            // Retrieve the metadata as a File object.
            val outputStream: OutputStream = FileOutputStream(targetFile)
            mDriveService.files()[fileId].executeMediaAndDownloadTo(outputStream)
            null
        }
    }

    fun deleteFolderFile(fileId: String?): Task<Void?>? {
        return call(mExecutor,
            {
                // Retrieve the metadata as a File object.
                if (fileId != null) {
                    mDriveService.files().delete(fileId).execute()
                }
                null
            })
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
    fun queryFiles(): Task<FileList> {
        return call(mExecutor) { mDriveService.files().list().setSpaces("drive").execute() }
    }



    fun uploadFile(
        localFile: java.io.File,
        mimeType: String?, folderId: String?
    ): Task<GoogleDriveFileHolder?>? {
        return call(mExecutor, { // Retrieve the metadata as a File object.
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
            googleDriveFileHolder
        })
    }

}