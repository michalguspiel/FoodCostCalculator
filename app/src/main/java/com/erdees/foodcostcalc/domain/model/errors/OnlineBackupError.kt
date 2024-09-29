package com.erdees.foodcostcalc.domain.model.errors

data object NoFileError : kotlin.Error("Can't find useful database in your google drive.") {
    private fun readResolve(): Any = NoFileError
}

data object SavingDatabaseFailure : kotlin.Error("Can't save database in your google drive.") {
    private fun readResolve(): Any = SavingDatabaseFailure
}

data object FailedToSignOut : kotlin.Error("Failed to sign out.") {
    private fun readResolve(): Any = FailedToSignOut
}

data object DrivePermissionFailure : kotlin.Error("Operation failed due to missing permissions") {
    private fun readResolve(): Any = DrivePermissionFailure
}