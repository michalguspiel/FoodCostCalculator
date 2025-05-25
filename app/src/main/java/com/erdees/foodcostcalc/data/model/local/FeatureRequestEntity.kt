package com.erdees.foodcostcalc.data.model.local

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.util.Date

// Type Converters for Date and FeatureRequestStatus
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}

@Keep
@Entity(tableName = "feature_requests")
@TypeConverters(Converters::class)
data class FeatureRequestEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val timestamp: Date
)

@Keep
@Entity(tableName = "upvoted_feature_requests")
data class UpvotedFeatureRequest(
    @PrimaryKey val id: String
)
