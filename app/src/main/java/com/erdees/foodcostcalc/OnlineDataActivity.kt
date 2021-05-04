package com.erdees.foodcostcalc

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.marginTop
import com.erdees.foodcostcalc.data.AppRoomDataBase
import com.erdees.foodcostcalc.drive.DriveServiceHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.gson.Gson
import java.io.*
import java.util.*


class OnlineDataActivity : AppCompatActivity() {

    private lateinit var signInButton: SignInButton
    private lateinit var testEmail: TextView
    private lateinit var signOutButton: Button
    private lateinit var saveDatabaseButton: Button
    private lateinit var loadButton: Button

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var googleDriveService: Drive
    private lateinit var mDriveServiceHelper: DriveServiceHelper

    private val RC_SIGN_IN = 213
    private var ACCESS_DRIVE_SCOPE: Scope = Scope(Scopes.DRIVE_FILE)
    private var SCOPE_EMAIL: Scope = Scope(Scopes.EMAIL)

    private lateinit var databaseFile: File

    override fun onStart() {
        super.onStart()
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            updateUI(account)
            checkForGooglePermissions()
            driveSetUp()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.online_data_activity)

        getDatabaseFileAndSaveItInVariable()
        setButtons()

        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions) // Check for existing Google Sign In account, if the user is already signed in the GoogleSignInAccount will be non-null.

        signInButton.setOnClickListener {
            signIn()
        }
        saveDatabaseButton.setOnClickListener {
            saveOrUpdateDatabaseInDrive()
        }
        signOutButton.setOnClickListener {
            signOut()
        }
        loadButton.setOnClickListener {
            loadFileFromDrive()
        }

    }


    private fun updateUI(account: GoogleSignInAccount?) {
        if (account != null) {
            testEmail.text = account.email
            signInButton.visibility = View.GONE
            signOutButton.visibility = View.VISIBLE
            saveDatabaseButton.isEnabled = true
            loadButton.isEnabled = true
        }
    }

    private fun signIn() {
        val signInIntent: Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun signOut() {
        mGoogleSignInClient.signOut().addOnCompleteListener {
            signOutButton.visibility = View.GONE
            signInButton.visibility = View.VISIBLE
            testEmail.text = "Sign in with google account to save and download your database."
            saveDatabaseButton.isEnabled = false
            loadButton.isEnabled = false
        }
    }




    private fun checkForGooglePermissions() {
        Log.w(TAG, "checking for permission")
        if (!GoogleSignIn.hasPermissions(
                GoogleSignIn.getLastSignedInAccount(applicationContext),
                ACCESS_DRIVE_SCOPE,
                SCOPE_EMAIL
            )
        ) {
            GoogleSignIn.requestPermissions(
                this@OnlineDataActivity,
                RC_SIGN_IN,
                GoogleSignIn.getLastSignedInAccount(applicationContext),
                ACCESS_DRIVE_SCOPE,
                SCOPE_EMAIL
            )
        } else {
            Toast.makeText(
                this,
                "Permission to access Drive and Email has been granted",
                Toast.LENGTH_SHORT
            ).show()
            driveSetUp()
        }
    }


    private fun driveSetUp() {
        val mAccount = GoogleSignIn.getLastSignedInAccount(this@OnlineDataActivity)
        val credential = GoogleAccountCredential.usingOAuth2(
            applicationContext, Collections.singleton(Scopes.DRIVE_FILE)
        )
        credential.selectedAccount = mAccount!!.account
        googleDriveService = Drive.Builder(
            AndroidHttp.newCompatibleTransport(),
            GsonFactory(),
            credential
        )
            .setApplicationName("Food Cost Calculator")
            .build()
        mDriveServiceHelper = DriveServiceHelper(googleDriveService)
    }


    private fun loadFileFromDrive() {
        val file = File(getExternalFilesDir(null), "product_database")
        mDriveServiceHelper.queryFiles().addOnSuccessListener { fileList ->
           val database = fileList.files.first()
            mDriveServiceHelper.downloadFile(file, database.id)?.addOnSuccessListener {
                    swapDatabaseFiles(file)
            }
        }
    }

    private fun swapDatabaseFiles(file: File){
        AppRoomDataBase.getDatabase(applicationContext).close()
        databaseFile.delete()
        copyFile(file.absolutePath, databaseFile.name, databaseFile.absolutePath)
        AppRoomDataBase.recreateDatabaseFromFile(applicationContext, file) // TO OPEN DATABASE
        val mainActivity = Intent(applicationContext, MainActivity::class.java)  // NOW RESTARTING ACTIVITY SO DATABASE IS REFRESHED
        mainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) //so by pressing back btn user doesn't come back to this activity
        startActivity(mainActivity)
    }



    private fun saveOrUpdateDatabaseInDrive() {
        mDriveServiceHelper.queryFiles().addOnSuccessListener { fileList ->
        if(fileList.files.isEmpty())saveDataBaseInDrive()
        else { updateDatabaseInDrive(fileList.files.first()) }
        }
    }

    private fun updateDatabaseInDrive(file: com.google.api.services.drive.model.File){
        mDriveServiceHelper.deleteFolderFile(file.id)?.addOnSuccessListener {
            Log.i(TAG, "File deleted!")
            saveDataBaseInDrive()
        }
    }

    private fun saveDataBaseInDrive(){
        mDriveServiceHelper.uploadFile(databaseFile, null, null)
            ?.addOnSuccessListener {
                val gson = Gson()
                Log.i(TAG, "onSuccess of save database " + gson.toJson(it))
            }
            ?.addOnFailureListener { Log.i(TAG, "onFailure of saving database " + it.message) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if(requestCode == RC_SIGN_IN) {
                val task: Task<GoogleSignInAccount> =
                    GoogleSignIn.getSignedInAccountFromIntent(data)
                handleSignInResult(task)
            }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            Log.i(TAG, "signInResult: SUCCESS !")
            // Signed in successfully, show authenticated UI.
            updateUI(account)
            driveSetUp()
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)
            updateUI(null)
            checkForGooglePermissions()
        }
    }

    private fun setButtons(){
        signInButton = findViewById(R.id.sign_in_button)
        saveDatabaseButton = findViewById(R.id.save_database_button)
        loadButton = findViewById(R.id.load_database_button)
        signOutButton = findViewById(R.id.sign_out_button)
        testEmail = findViewById(R.id.test_sign_in) // TODO TO ERASE
    }

    private fun getDatabaseFileAndSaveItInVariable() {
        databaseFile = getDatabasePath("product_database").absoluteFile
    }

    companion object {
        const val TAG = "OnlineDataActivity"
    }


    private fun copyFile(inputPath: String, inputFileName: String, outputPath: String) {
        var inputStream: InputStream?
        var out: OutputStream?
        try {
            inputStream = FileInputStream(inputPath)
            out = FileOutputStream(outputPath + inputFileName)
            val buffer = ByteArray(1024)
            var read: Int
            while (inputStream.read(buffer).also { read = it } != -1) {
                out.write(buffer, 0, read)
            }
            inputStream.close()
            // write the output file (You have now copied the file)
            out.flush()
            out.close()
        } catch (fnfe1: FileNotFoundException) {
            fnfe1.message?.let { Log.e("tag fnfe1", it)
            }
        } catch (e: Exception) {
            Log.e("tag exception", e.message!!)
        }
    }


}