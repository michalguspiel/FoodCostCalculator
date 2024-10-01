package com.erdees.foodcostcalc.domain.model.errors

data object NoDatabaseFileError : kotlin.Error("Can't find database file in your google drive.") {
    private fun readResolve(): Any = NoDatabaseFileError
}

data object DriveQueryFailure : kotlin.Error("Can't query files from your google drive.") {
    private fun readResolve(): Any = DriveQueryFailure
}

data object DownloadFailure : kotlin.Error("Failed to download database from your google drive.") {
    private fun readResolve(): Any = DownloadFailure
}

data object SavingDatabaseFailure : kotlin.Error("Can't save database in your google drive.") {
    private fun readResolve(): Any = SavingDatabaseFailure
}

data object DriveSetupFailure : kotlin.Error("Failed to setup google drive.") {
    private fun readResolve(): Any = DriveSetupFailure
}

data object FailedToSignOut : kotlin.Error("Failed to sign out.") {
    private fun readResolve(): Any = FailedToSignOut
}

data object DrivePermissionFailure : kotlin.Error("Operation failed due to missing permissions") {
    private fun readResolve(): Any = DrivePermissionFailure
}