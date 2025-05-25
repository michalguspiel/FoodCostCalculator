package com.erdees.foodcostcalc.data.model.remote

sealed class FirestoreResult<out T> {
    data class Success<out T>(val data: T) : FirestoreResult<T>()
    data class Error(val exception: Exception) : FirestoreResult<Nothing>()
}
