package com.erdees.foodcostcalc.data.model.remote

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import timber.log.Timber
import java.util.Date

data class FeatureRequest(
    @DocumentId var id: String? = null,
    val title: String = "",
    val description: String = "",
    val approved: Boolean = false,
    val upVotes : Int = 0,
    @ServerTimestamp var timestamp: Date? = null,
    val status: String = FeatureRequestStatus.PENDING.name
) {
    @Exclude // Exclude this from Firestore serialization if it's purely for client side
    fun getStatusEnum(): FeatureRequestStatus {
        return try {
            Timber.d("Getting status enum for $status")
            FeatureRequestStatus.valueOf(status.uppercase())
        } catch (e: IllegalArgumentException) {
            FeatureRequestStatus.UNKNOWN
        }
    }
}