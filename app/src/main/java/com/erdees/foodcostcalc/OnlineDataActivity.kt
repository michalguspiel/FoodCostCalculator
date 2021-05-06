package com.erdees.foodcostcalc

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.erdees.foodcostcalc.SharedFunctions.makeGone
import com.erdees.foodcostcalc.SharedFunctions.makeVisible
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
    private lateinit var emailTV: TextView
    private lateinit var welcomeTV : TextView
    private lateinit var signOutButton: Button
    private lateinit var alertDialog: AlertDialog
    private lateinit var resultAlertDialog: AlertDialog
    private lateinit var saveDatabaseButton: Button
    private lateinit var constraintLayoutStateSignOut : ConstraintLayout
    private lateinit var loadButton: Button
    private lateinit var profilePic: ImageView
    private lateinit var profilePicLayout : FrameLayout
    private lateinit var signingLayout: ConstraintLayout
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
        bindView()


        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(
            this,
            googleSignInOptions
        ) // Check for existing Google Sign In account, if the user is already signed in the GoogleSignInAccount will be non-null.

        signInButton.setOnClickListener {
            signIn()
        }
        saveDatabaseButton.setOnClickListener {
            if(accountDoesNotHaveGooglePermissions())showResultDialog(false,"Error! Can't do that without permissions.")
            else saveOrUpdateDatabaseInDrive()
        }
        signOutButton.setOnClickListener {
            signOut()
        }
        loadButton.setOnClickListener {
            if(accountDoesNotHaveGooglePermissions())showResultDialog(false,"Error! Can't do that without permissions.")
            else loadFileFromDrive()
        }

    }

    private fun bindView() {
        signInButton = findViewById(R.id.sign_in_button)
        saveDatabaseButton = findViewById(R.id.save_database_button)
        loadButton = findViewById(R.id.load_database_button)
        signOutButton = findViewById(R.id.sign_out_button)
        emailTV = findViewById(R.id.email_tv)
        profilePic = findViewById(R.id.profile_picture)
        profilePicLayout = findViewById(R.id.profile_picture_layout)
        signingLayout = findViewById(R.id.signing_layout)
        constraintLayoutStateSignOut = findViewById(R.id.constraintLayout2_state_signout)
        welcomeTV = findViewById(R.id.welcome_tv)
    }

    private fun updateUI(account: GoogleSignInAccount?) {
        if (account != null) {
            emailTV.text = account.email
            emailTV.makeVisible()
            signOutButton.makeVisible()
            signingLayout.makeVisible()
            welcomeTV.makeVisible()
            profilePicLayout.makeVisible()
            constraintLayoutStateSignOut.makeGone()
            signInButton.makeGone()
            saveDatabaseButton.isEnabled = true
            loadButton.isEnabled = true
            if (account.photoUrl != null) setPicture(account.photoUrl!!, profilePic)
        }
    }

    private fun signOut() {
        mGoogleSignInClient.signOut().addOnCompleteListener {
            signOutButton.makeGone()
            signingLayout.makeGone()
            welcomeTV.makeGone()
            signInButton.makeVisible()
            constraintLayoutStateSignOut.makeVisible()
            emailTV.text = ""
            saveDatabaseButton.isEnabled = false
            loadButton.isEnabled = false
            Glide.with(this).clear(profilePic)
            profilePicLayout.visibility = View.INVISIBLE
        }
    }

    private fun signIn() {
        val signInIntent: Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun accountDoesNotHaveGooglePermissions(): Boolean {
        return !GoogleSignIn.hasPermissions(
            GoogleSignIn.getLastSignedInAccount(applicationContext),
            ACCESS_DRIVE_SCOPE,
            SCOPE_EMAIL
        )
    }

    private fun checkForGooglePermissions() {
        Log.w(TAG, "checking for permission")
        if (accountDoesNotHaveGooglePermissions()) {
            GoogleSignIn.requestPermissions(
                this@OnlineDataActivity,
                RC_SIGN_IN,
                GoogleSignIn.getLastSignedInAccount(applicationContext),
                ACCESS_DRIVE_SCOPE,
                SCOPE_EMAIL
            )
        } else {
            Log.i(TAG, "Permission to access Drive And email has been granted.")
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
        showLoadingDialog()
        val file = File(getExternalFilesDir(null), "product_database")
        mDriveServiceHelper.queryFiles().addOnSuccessListener { fileList ->
            if(fileList.files.isEmpty()){
                alertDialog.dismiss()
                showResultDialog(false,"Can't find useful database in your google drive.")
                return@addOnSuccessListener
            }
            val database = fileList.files.first()
            mDriveServiceHelper.downloadFile(file, database.id)?.addOnSuccessListener {
                swapDatabaseFiles(file)
            }
                ?.addOnFailureListener {
                    alertDialog.dismiss()
                    showResultDialog(false,"Can't find useful database in your google drive.")
                }
        }
    }

    private fun swapDatabaseFiles(file: File) {
        AppRoomDataBase.getDatabase(applicationContext).close()
        databaseFile.delete()
        copyFile(file.absolutePath, databaseFile.name, databaseFile.absolutePath)
        AppRoomDataBase.recreateDatabaseFromFile(applicationContext, file) // TO OPEN DATABASE
        val mainActivity = Intent(
            applicationContext,
            MainActivity::class.java
        )  // NOW RESTARTING ACTIVITY SO DATABASE IS REFRESHED
        mainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) //so by pressing back btn user doesn't come back to this activity
        alertDialog.dismiss()
        startActivity(mainActivity)
    }

    private fun showLoadingDialog() {
        val progressBar = this.layoutInflater.inflate(R.layout.loading_bar, null)
        alertDialog = AlertDialog.Builder(this)
            .setView(progressBar)
            .show()
        alertDialog.window?.setBackgroundDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.background_for_dialogs
            )
        )
    }

    private fun showResultDialog(wasSuccessful: Boolean, error : String?) {
        var message = ""
        message = if(wasSuccessful) "Your database was successfully saved in google drive."
        else "Something went wrong, your database wasn't saved."
        if(error != null) message = error
       resultAlertDialog =  AlertDialog.Builder(this)
           .setMessage(message)
           .setNegativeButton("Back",null)
           .create()
        resultAlertDialog.window?.setBackgroundDrawable(
            ContextCompat.getDrawable(this,R.drawable.background_for_dialogs)
        )
        resultAlertDialog.show()
    }


    private fun saveOrUpdateDatabaseInDrive() {
        showLoadingDialog()
        mDriveServiceHelper.queryFiles().addOnSuccessListener { fileList ->
            if (fileList.files.isEmpty()) saveDataBaseInDrive()
            else {
                updateDatabaseInDrive(fileList.files.first())
            }
        }
    }

    private fun updateDatabaseInDrive(file: com.google.api.services.drive.model.File) {
        mDriveServiceHelper.deleteFolderFile(file.id)?.addOnSuccessListener {
            Log.i(TAG, "File deleted!")
            saveDataBaseInDrive()
        }
    }

    private fun saveDataBaseInDrive() {
        mDriveServiceHelper.uploadFile(databaseFile, null, null)
            ?.addOnSuccessListener {
                val gson = Gson()
                Log.i(TAG, "onSuccess of save database " + gson.toJson(it))
                alertDialog.dismiss()
                showResultDialog(true,null)
            }
            ?.addOnFailureListener {
                Log.i(TAG, "onFailure of saving database " + it.message)
                alertDialog.dismiss()
            showResultDialog(wasSuccessful = false,null)
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
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
            checkForGooglePermissions()
            driveSetUp()
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)
            updateUI(null)
          //  checkForGooglePermissions()
        }
    }



    private fun getDatabaseFileAndSaveItInVariable() {
        databaseFile = getDatabasePath("product_database").absoluteFile
    }

    private fun setPicture(imageUrl: Uri, image: ImageView) {
        Glide.with(this)
            .load(imageUrl)
            .circleCrop()
            .into(image)
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
            fnfe1.message?.let {
                Log.e("tag fnfe1", it)
            }
        } catch (e: Exception) {
            Log.e("tag exception", e.message!!)
        }
    }


}