package com.erdees.foodcostcalc.data.db.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration from version 6 to 7: Refine ProductBase schema
 * - Add input_method, package_price, package_quantity, package_unit fields
 * - Rename pricePerUnit to canonical_price and unit to canonical_unit
 */
val Migration_6to7_ProductBaseSchema = object : Migration(6, 7) {
    override fun migrate(database: SupportSQLiteDatabase) {

        // Step 1: Create new products table with the updated schema
        database.execSQL(
            """
            CREATE TABLE products_new (
                productId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                product_name TEXT NOT NULL,
                input_method TEXT NOT NULL DEFAULT 'UNIT',
                package_price REAL,
                package_quantity REAL,
                package_unit TEXT,
                canonical_price REAL NOT NULL,
                canonical_unit TEXT NOT NULL,
                tax REAL NOT NULL,
                waste REAL NOT NULL
            )
            """.trimIndent()
        )

        // Step 2: Copy data from old table to new table
        // For existing rows:
        // - input_method defaults to 'UNIT'
        // - package_* fields are NULL
        // - pricePerUnit becomes canonical_price
        // - unit becomes canonical_unit
        database.execSQL(
            """
            INSERT INTO products_new (
                productId, 
                product_name, 
                input_method,
                package_price,
                package_quantity,
                package_unit,
                canonical_price,
                canonical_unit,
                tax, 
                waste
            )
            SELECT 
                productId, 
                product_name, 
                'UNIT' as input_method,
                NULL as package_price,
                NULL as package_quantity,
                NULL as package_unit,
                pricePerUnit as canonical_price,
                unit as canonical_unit,
                tax, 
                waste
            FROM products
            """.trimIndent()
        )

        // Step 3: Drop the old table
        database.execSQL("DROP TABLE products")

        // Step 4: Rename the new table to the original name
        database.execSQL("ALTER TABLE products_new RENAME TO products")
    }
}
