package com.erdees.foodcostcalc.data.db.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class Migration_3_to_4_AddMethodDescription : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add the new column to the "dishes" table
        db.execSQL("ALTER TABLE dishes ADD COLUMN method_description TEXT")
    }
}