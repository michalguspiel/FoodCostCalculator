package com.erdees.foodcostcalc.data.db.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Suppress("ClassName", "MagicNumber")
class Migration_4to_5_FeatureRequests : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {

        // Enable foreign keys
        db.execSQL("PRAGMA foreign_keys = ON;")

        // Create new FeatureRequest table
        db.execSQL(
            """
                CREATE TABLE IF NOT EXISTS `feature_requests` (
                    `id` TEXT PRIMARY KEY NOT NULL,
                    `title` TEXT NOT NULL,
                    `description` TEXT NOT NULL,
                    `timestamp` INTEGER NOT NULL
                )
            """.trimIndent()
        )

        // Create new UpvotedFeatureRequest table
        db.execSQL(
            """
                CREATE TABLE IF NOT EXISTS `upvoted_feature_requests` (
                    `id` TEXT PRIMARY KEY NOT NULL
            )        
            """.trimIndent()
        )
    }
}