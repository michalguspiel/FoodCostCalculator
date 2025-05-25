package com.erdees.foodcostcalc.data.model.remote

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class FeatureRequest(
    @DocumentId var id: String? = null,
    val title: String = "",
    val description: String = "",
    val approved: Boolean = false,
    val status: FeatureRequestStatus = FeatureRequestStatus.PENDING,
    val upVotes : Int = 0,
    @ServerTimestamp var timestamp: Date? = null
)