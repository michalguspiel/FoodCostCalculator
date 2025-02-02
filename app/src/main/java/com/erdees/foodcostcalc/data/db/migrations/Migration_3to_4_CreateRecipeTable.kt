package com.erdees.foodcostcalc.data.db.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class Migration_3to_4_CreateRecipeTable : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {

        // Step 0: Enable foreign keys
        db.execSQL("PRAGMA foreign_keys = ON;")

        // Step 1: Create the new "Recipe" table
        db.execSQL(
            """
                CREATE TABLE IF NOT EXISTS `Recipe` (
                    `recipeId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `prepTimeMinutes` INTEGER,
                    `cookTimeMinutes` INTEGER,
                    `description` TEXT,
                    `tips` TEXT
                )
            """.trimIndent()
        )

        // Step 2: Create the new "Recipe_Step" table
        db.execSQL(
            """
                CREATE TABLE IF NOT EXISTS `Recipe_Step` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `recipeId` INTEGER NOT NULL,
                    `stepDescription` TEXT NOT NULL,
                    `order` INTEGER NOT NULL,
                     FOREIGN KEY(`recipeId`) REFERENCES `Recipe`(`recipeId`) ON DELETE CASCADE
                )
            """.trimIndent()
        )
        // Create index
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_Recipe_Step_recipeId` ON `Recipe_Step` (`recipeId`)"
        )

        // Step 3: Add "recipeId" column to "dishes" table
        db.execSQL("ALTER TABLE `dishes` ADD COLUMN `recipeId` INTEGER")


        // Step4: Add Foreign key to dishes table

        // 1. Create a new "dishes_new" table with the foreign key.
        // 2. Copy the data from the old table to the new table.
        // 3. Drop the old table.
        // 4. Rename the new table to "dishes".

        db.execSQL(
            """
                CREATE TABLE `dishes_new` (
                    `dishId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `dish_name` TEXT NOT NULL,
                    `margin_percent` REAL NOT NULL DEFAULT 100.0,
                    `dish_tax` REAL NOT NULL DEFAULT 0.0,
                    `recipeId` INTEGER,
                    FOREIGN KEY(`recipeId`) REFERENCES `Recipe`(`recipeId`) ON DELETE CASCADE
                    )
            """
                .trimIndent()
        )
        db.execSQL("INSERT INTO `dishes_new` SELECT * FROM `dishes`")
        db.execSQL("DROP TABLE `dishes`")
        db.execSQL("ALTER TABLE `dishes_new` RENAME TO `dishes`")

        // Create Index
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_dishes_recipeId` ON `dishes`(`recipeId`)")
    }
}