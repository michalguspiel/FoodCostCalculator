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

    /**
     * Creates a text file in the user's My Drive folder and returns its file ID.
     */
    fun createFile(): Task<String> {
        return call(mExecutor) {
            val metadata: File = File()
                .setParents(Collections.singletonList("root"))
                .setMimeType("text/plain")
                .setName("Untitled file")
            val googleFile: File = mDriveService.files().create(metadata).execute()
                ?: throw IOException("Null result when requesting file creation.")
            googleFile.id
        }
    }

    /**
     * Opens the file identified by `fileId` and returns a [Pair] of its name and
     * contents.
     */
    fun readFile(fileId: String?): Task<Pair<String, String>> {
        return call(mExecutor) {

            // Retrieve the metadata as a File object.
            val metadata: File = mDriveService.files().get(fileId).execute()
            val name: String = metadata.name
            mDriveService.files().get(fileId).executeMediaAsInputStream().use { `is` ->
                BufferedReader(InputStreamReader(`is`)).use { reader ->
                    val stringBuilder = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        stringBuilder.append(line)
                    }
                    val contents = stringBuilder.toString()
                    return@call Pair(name, contents)
                }
            }
        }
    }

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

    /**
     * Returns an [Intent] for opening the Storage Access Framework file picker.
     */
    fun createFilePickerIntent(): Intent {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "text/plain"
        return intent
    }

    /**
     * Opens the file at the `uri` returned by a Storage Access Framework [Intent]
     * created by [.createFilePickerIntent] using the given `contentResolver`.
     */
    fun openFileUsingStorageAccessFramework(
        contentResolver: ContentResolver, uri: Uri?
    ): Task<Pair<String, String>> {
        return call(mExecutor) {

            // Retrieve the document's display name from its metadata.
            var name: String
            contentResolver.query(uri!!, null, null, null, null).use { cursor ->
                name = if (cursor != null && cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    cursor.getString(nameIndex)
                } else {
                    throw IOException("Empty cursor returned for file.")
                }
            }

            // Read the document's contents as a String.
            var content: String
            contentResolver.openInputStream(uri).use { `is` ->
                BufferedReader(InputStreamReader(`is`)).use { reader ->
                    val stringBuilder = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        stringBuilder.append(line)
                    }
                    content = stringBuilder.toString()
                }
            }
            Pair(name, content)
        }
    }



    // TO CREATE A FOLDER
    fun createFolder(
        folderName: String?,
        @Nullable folderId: String?
    ): Task<GoogleDriveFileHolder>? {
        return call(mExecutor,
            {
                val googleDriveFileHolder = GoogleDriveFileHolder()
                val root: List<String> = folderId?.let { listOf(it) } ?: listOf("root")
                val metadata =
                    File()
                        .setParents(root)
                        .setMimeType("application/vnd.google-apps.folder")
                        .setName(folderName)
                val googleFile =
                    mDriveService.files().create(metadata).execute()
                        ?: throw IOException("Null result when requesting file creation.")
                googleDriveFileHolder.id = googleFile.id
                googleDriveFileHolder
            })
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