package com.erdees.foodcostcalc.domain.model

import android.net.Uri
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

data class FCCUser(
    val googleSignInAccount: GoogleSignInAccount,
    val email: String,
    val profilePicUrl: Uri?
)