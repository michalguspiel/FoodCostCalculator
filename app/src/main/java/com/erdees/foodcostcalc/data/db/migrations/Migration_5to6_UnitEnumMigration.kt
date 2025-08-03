package com.erdees.foodcostcalc.data.db.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.erdees.foodcostcalc.domain.model.units.MeasurementUnit

/**
 * Migration from String-based units to MeasurementUnit enum
 * Version 5 to 6: Convert string units to enum values
 */
val Migration_5to6_UnitEnumMigration = object : Migration(5, 6){
    override fun migrate(database: SupportSQLiteDatabase) {

        // Step 1: Add temporary columns for new enum-based units
        database.execSQL("ALTER TABLE products ADD COLUMN unit_new TEXT DEFAULT 'GRAM'")
        database.execSQL("ALTER TABLE HalfProduct ADD COLUMN halfProductUnit_new TEXT DEFAULT 'GRAM'")
        database.execSQL("ALTER TABLE Product_Dish ADD COLUMN quantityUnit_new TEXT DEFAULT 'GRAM'")
        database.execSQL("ALTER TABLE Product_HalfProduct ADD COLUMN quantityUnit_new TEXT DEFAULT 'GRAM'")
        database.execSQL("ALTER TABLE HalfProduct_Dish ADD COLUMN quantityUnit_new TEXT DEFAULT 'GRAM'")

        // Step 2: Map existing string units to enum values - ONLY existing units
        val unitMappings = mapOf(
            // Weight units - existing in your system
            "kg" to MeasurementUnit.KILOGRAM.name,
            "kilogram" to MeasurementUnit.KILOGRAM.name,
            "per kilogram" to MeasurementUnit.KILOGRAM.name,
            "g" to MeasurementUnit.GRAM.name,
            "gram" to MeasurementUnit.GRAM.name,
            "per gram" to MeasurementUnit.GRAM.name,
            "lb" to MeasurementUnit.POUND.name,
            "pound" to MeasurementUnit.POUND.name,
            "per pound" to MeasurementUnit.POUND.name,
            "oz" to MeasurementUnit.OUNCE.name,
            "ounce" to MeasurementUnit.OUNCE.name,
            "per ounce" to MeasurementUnit.OUNCE.name,

            // Volume units - existing in your system
            "l" to MeasurementUnit.LITER.name,
            "liter" to MeasurementUnit.LITER.name,
            "per liter" to MeasurementUnit.LITER.name,
            "ml" to MeasurementUnit.MILLILITER.name,
            "milliliter" to MeasurementUnit.MILLILITER.name,
            "per milliliter" to MeasurementUnit.MILLILITER.name,
            "fl oz" to MeasurementUnit.FLUID_OUNCE.name,
            "fluid ounce" to MeasurementUnit.FLUID_OUNCE.name,
            "gal" to MeasurementUnit.GALLON.name,
            "gallon" to MeasurementUnit.GALLON.name,
            "per gallon" to MeasurementUnit.GALLON.name,

            // Count units - existing in your system
            "piece" to MeasurementUnit.PIECE.name,
            "per piece" to MeasurementUnit.PIECE.name,
            "pcs" to MeasurementUnit.PIECE.name
        )

        // Step 3: Update each table with mapped units
        updateUnitsInTable(database, "products", "unit", "unit_new", unitMappings)
        updateUnitsInTable(database, "HalfProduct", "halfProductUnit", "halfProductUnit_new", unitMappings)
        updateUnitsInTable(database, "Product_Dish", "quantityUnit", "quantityUnit_new", unitMappings)
        updateUnitsInTable(database, "Product_HalfProduct", "quantityUnit", "quantityUnit_new", unitMappings)
        updateUnitsInTable(database, "HalfProduct_Dish", "quantityUnit", "quantityUnit_new", unitMappings)

        // Step 4: Drop old columns and rename new ones

        // Products table
        database.execSQL(
            """
            CREATE TABLE products_new (
                productId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                product_name TEXT NOT NULL,
                pricePerUnit REAL NOT NULL,
                tax REAL NOT NULL,
                waste REAL NOT NULL,
                unit TEXT NOT NULL
            )
            """.trimIndent()
        )
        database.execSQL("INSERT INTO products_new SELECT productId, product_name, pricePerUnit, tax, waste, unit_new FROM products")
        database.execSQL("DROP TABLE products")
        database.execSQL("ALTER TABLE products_new RENAME TO products")

        // HalfProduct table - Fix: Use explicit CREATE with constraints
        database.execSQL(
            """
            CREATE TABLE HalfProduct_new (
                halfProductId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                halfProductUnit TEXT NOT NULL
            )
            """.trimIndent()
        )
        database.execSQL("INSERT INTO HalfProduct_new SELECT halfProductId, name, halfProductUnit_new FROM HalfProduct")
        database.execSQL("DROP TABLE HalfProduct")
        database.execSQL("ALTER TABLE HalfProduct_new RENAME TO HalfProduct")

        // Product_Dish table - Fix: Use explicit CREATE with constraints
        database.execSQL(
            """
            CREATE TABLE Product_Dish_new (
                productDishId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                productId INTEGER NOT NULL,
                dishId INTEGER NOT NULL,
                quantity REAL NOT NULL,
                quantityUnit TEXT NOT NULL,
                FOREIGN KEY(productId) REFERENCES products(productId) ON DELETE CASCADE,
                FOREIGN KEY(dishId) REFERENCES dishes(dishId) ON DELETE CASCADE
            )
            """.trimIndent()
        )
        database.execSQL("INSERT INTO Product_Dish_new SELECT productDishId, productId, dishId, quantity, quantityUnit_new FROM Product_Dish")
        database.execSQL("DROP TABLE Product_Dish")
        database.execSQL("ALTER TABLE Product_Dish_new RENAME TO Product_Dish")

        // Product_HalfProduct table - Fix: Use explicit CREATE with constraints
        database.execSQL(
            """
            CREATE TABLE Product_HalfProduct_new (
                productHalfProductId INTEGER PRIMARY KEY NOT NULL,
                productId INTEGER NOT NULL,
                halfProductId INTEGER NOT NULL,
                quantity REAL NOT NULL,
                quantityUnit TEXT NOT NULL,
                weightPiece REAL,
                FOREIGN KEY(productId) REFERENCES products(productId) ON DELETE CASCADE,
                FOREIGN KEY(halfProductId) REFERENCES HalfProduct(halfProductId) ON DELETE CASCADE
            )
            """.trimIndent()
        )
        database.execSQL("INSERT INTO Product_HalfProduct_new SELECT productHalfProductId, productId, halfProductId, quantity, quantityUnit_new, weightPiece FROM Product_HalfProduct")
        database.execSQL("DROP TABLE Product_HalfProduct")
        database.execSQL("ALTER TABLE Product_HalfProduct_new RENAME TO Product_HalfProduct")

        // HalfProduct_Dish table - Fix: Use explicit CREATE with constraints
        database.execSQL(
            """
            CREATE TABLE HalfProduct_Dish_new (
                halfProductDishId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                halfProductId INTEGER NOT NULL,
                dishId INTEGER NOT NULL,
                quantity REAL NOT NULL,
                quantityUnit TEXT NOT NULL,
                FOREIGN KEY(halfProductId) REFERENCES HalfProduct(halfProductId) ON DELETE CASCADE,
                FOREIGN KEY(dishId) REFERENCES dishes(dishId) ON DELETE CASCADE
            )
            """.trimIndent()
        )
        database.execSQL("INSERT INTO HalfProduct_Dish_new SELECT halfProductDishId, halfProductId, dishId, quantity, quantityUnit_new FROM HalfProduct_Dish")
        database.execSQL("DROP TABLE HalfProduct_Dish")
        database.execSQL("ALTER TABLE HalfProduct_Dish_new RENAME TO HalfProduct_Dish")

        // Step 5: Recreate indexes
        database.execSQL("CREATE INDEX IF NOT EXISTS index_Product_Dish_productId ON Product_Dish(productId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_Product_Dish_dishId ON Product_Dish(dishId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_Product_HalfProduct_productId ON Product_HalfProduct(productId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_Product_HalfProduct_halfProductId ON Product_HalfProduct(halfProductId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_HalfProduct_Dish_halfProductId ON HalfProduct_Dish(halfProductId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_HalfProduct_Dish_dishId ON HalfProduct_Dish(dishId)")
    }

    private fun updateUnitsInTable(
        database: SupportSQLiteDatabase,
        tableName: String,
        oldColumn: String,
        newColumn: String,
        mappings: Map<String, String>
    ) {
        for ((oldUnit, newUnit) in mappings) {
            database.execSQL("""
                UPDATE $tableName 
                SET $newColumn = '$newUnit' 
                WHERE LOWER(TRIM($oldColumn)) = '${oldUnit.lowercase()}'
            """)
        }

        // Set any remaining unmapped units to GRAM as fallback
        database.execSQL("""
            UPDATE $tableName 
            SET $newColumn = '${MeasurementUnit.GRAM.name}' 
            WHERE $newColumn = '${MeasurementUnit.GRAM.name}' AND 
                  LOWER(TRIM($oldColumn)) NOT IN (${mappings.keys.joinToString(",") { "'${it.lowercase()}'" }})
        """)
    }
}
