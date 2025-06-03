package com.erdees.foodcostcalc.domain.model

import com.erdees.foodcostcalc.data.model.remote.FeatureRequestStatus

data class FeatureRequestDomain(
    val id: String,
    val title: String,
    val description: String,
    val status: FeatureRequestStatus = FeatureRequestStatus.PENDING,
    val upVotes : Int = 0,
    val formattedTimeStamp: String
)