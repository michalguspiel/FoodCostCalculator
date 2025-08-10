package com.erdees.foodcostcalc

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.erdees.foodcostcalc.data.AppRoomDataBase
import com.erdees.foodcostcalc.data.db.migrations.Migration_1to2_RefactorDatabase
import com.erdees.foodcostcalc.data.db.migrations.Migration_2to3_Remove_Ref_Tables_Where_Ref_Does_Not_Exist
import com.erdees.foodcostcalc.data.db.migrations.Migration_5to6_UnitEnumMigration
import com.erdees.foodcostcalc.data.db.migrations.Migration_6to7_ProductBaseSchema
import com.erdees.foodcostcalc.domain.model.units.MeasurementUnit
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4ClassRunner::class)
class MigrationTest {
    private lateinit var helper: MigrationTestHelper

    @Before
    fun setUp() {
        helper = MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            AppRoomDataBase::class.java,
            listOf(),
            FrameworkSQLiteOpenHelperFactory()
        )
    }

    @Test
    @Throws(IOException::class)
    fun testMigration_1to2_RefactorDatabase() {
        // Create the database with version 1
        helper.createDatabase(TEST_DB, 1).apply {
            // Insert some data
            execSQL(
                """
        INSERT INTO ProductIncluded (productIncludedId, productOwnerId, dishOwnerId, weight, weightUnit, productId, product_name, pricePerUnit, tax, waste, unit, dishId, dish_name, margin_percent, dish_tax)
        VALUES (1, 1, 1, 100.0, 'g', 1, 'Product Name', 10.0, 5.0, 2.0, 'kg',1 ,'Dish Name', 100.0, 0.0)
    """.trimIndent()
            )
            execSQL(
                """
      INSERT INTO HalfProductIncludedInDish (halfProductOwnerId, dishOwnerId, weight, unit, dishId, dish_name, margin_percent, dish_tax,halfProductId, name, halfProductUnit)
      VALUES (1, 1, 100.0, 'g',1,'Dish Name', 100.0, 0.0, 1, 'Half Product Name', 'kg')
      """.trimIndent()
            )

            // Insert some data into ProductIncludedInHalfProduct
            execSQL(
                """
      INSERT INTO ProductIncludedInHalfProduct (halfProductHostId, weight, weightUnit, weightOfPiece,productId, product_name, pricePerUnit, tax, waste, unit,halfProductId, name, halfProductUnit)
      VALUES (1, 100.0, 'g', 50.0, 1, 'Product Name', 10.0, 5.0, 2.0, 'kg',1, 'Half Product Name', 'kg')
      """.trimIndent()
            )

            close()
        }

        // Migrate the database to version 2
        helper.runMigrationsAndValidate(TEST_DB, 2, true, Migration_1to2_RefactorDatabase())
    }

    @Test
    @Throws(IOException::class)
    fun testMigration_2_to_3() {
        // Create the database with version 2 schema and insert test data
        helper.createDatabase(TEST_DB, 2).apply {
            execSQL("INSERT INTO Product_Dish (productDishId, productId, dishId, quantity, quantityUnit) VALUES (1, 999, 1, 1.0, 'kg')")
            execSQL("INSERT INTO Product_HalfProduct (productHalfProductId, productId, halfProductId, quantity, quantityUnit, weightPiece) VALUES (1, 999, 1, 1.0, 'kg', 1.0)")
            execSQL("INSERT INTO HalfProduct_Dish (halfProductDishId, halfProductId, dishId, quantity, quantityUnit) VALUES (1, 999, 1, 1.0, 'kg')")
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 3, true, Migration_2to3_Remove_Ref_Tables_Where_Ref_Does_Not_Exist())

        // Verify that the entries with non-existent foreign keys are removed
        db.apply {
            val cursor1 = query("SELECT * FROM Product_Dish WHERE productId = 999")
            assert(cursor1.count == 0)
            cursor1.close()

            val cursor2 = query("SELECT * FROM Product_HalfProduct WHERE productId = 999")
            assert(cursor2.count == 0)
            cursor2.close()

            val cursor3 = query("SELECT * FROM HalfProduct_Dish WHERE halfProductId = 999")
            assert(cursor3.count == 0)
            cursor3.close()

            close()
        }
    }

    @Test
    fun testMigration_5_to_6() {
        // Create the database with version 5 schema and insert real-world test data
        helper.createDatabase(TEST_DB, 5).apply {
            // Insert real product data from the app - comprehensive unit coverage
            execSQL("""
                INSERT INTO products (productId, product_name, pricePerUnit, tax, waste, unit) 
                VALUES 
                (1, 'Minced Beef', 19.2, 0, 0, 'per kilogram'),
                (2, 'Burger Bun', 0.7, 0, 0, 'per piece'),
                (3, 'Cheese Slice', 0.5, 0, 0, 'per piece'),
                (4, 'Lettuce', 3.99, 0, 15, 'per kilogram'),
                (5, 'Milk', 1, 0, 0, 'per liter'),
                (6, 'Oil', 10, 0, 0, 'per gallon'),
                (7, 'Flour', 1, 0, 0, 'per pound'),
                (8, 'Test Gram Product', 5.0, 2.5, 0, 'g'),
                (9, 'Test ML Product', 3.0, 1.0, 5, 'ml'),
                (10, 'Test Ounce Product', 8.0, 0, 10, 'oz'),
                (11, 'Test Fluid Ounce Product', 4.5, 3.0, 0, 'fl oz'),
                (12, 'Test Piece Product', 1.25, 5.0, 2, 'pcs')
            """.trimIndent())

            // Insert dishes for the foreign key relationships
            execSQL("""
                INSERT INTO dishes (dishId, dish_name, margin_percent, dish_tax) 
                VALUES 
                (1, 'Burger Dish', 100.0, 0.0),
                (2, 'Complex Multi-Unit Dish', 120.0, 5.0)
            """.trimIndent())

            // Insert real HalfProduct data - comprehensive unit coverage
            execSQL("""
                INSERT INTO HalfProduct (halfProductId, name, halfProductUnit) 
                VALUES 
                (1, 'Piece Mix', 'per piece'),
                (2, 'Kg Mix', 'per kilogram'),
                (3, 'Liter Mix', 'per liter'),
                (4, 'Pound Mix', 'per pound'),
                (5, 'Gallon Mix', 'per gallon'),
                (6, 'Gram Mix', 'g'),
                (7, 'ML Mix', 'ml'),
                (8, 'Ounce Mix', 'oz'),
                (9, 'Fluid Ounce Mix', 'fl oz')
            """.trimIndent())

            // Insert comprehensive Product_Dish data covering all unit types
            execSQL("""
                INSERT INTO Product_Dish (productDishId, productId, dishId, quantity, quantityUnit) 
                VALUES 
                (1, 1, 1, 150, 'gram'),
                (2, 2, 1, 1, 'piece'),
                (3, 3, 1, 1, 'piece'),
                (4, 4, 1, 20, 'gram'),
                (5, 7, 2, 1, 'gram'),
                (6, 7, 2, 1, 'ounce'),
                (7, 7, 2, 1, 'ounce'),
                (8, 7, 2, 1, 'ounce'),
                (9, 5, 2, 1, 'fluid ounce'),
                (10, 5, 2, 0.1, 'gallon'),
                (11, 6, 2, 20, 'milliliter'),
                (12, 8, 2, 100, 'g'),
                (13, 9, 2, 500, 'ml'),
                (14, 10, 2, 2, 'oz'),
                (15, 11, 2, 8, 'fl oz'),
                (16, 12, 2, 5, 'pcs')
            """.trimIndent())

            // Insert comprehensive Product_HalfProduct data
            execSQL("""
                INSERT INTO Product_HalfProduct (productHalfProductId, productId, halfProductId, quantity, quantityUnit, weightPiece) 
                VALUES 
                (1, 7, 5, 1, 'kilogram', 1),
                (2, 7, 2, 1, 'pound', 1),
                (3, 7, 2, 1, 'gram', 1),
                (4, 7, 2, 1, 'gram', 1),
                (5, 6, 5, 2, 'fluid ounce', 1),
                (6, 5, 1, 20, 'milliliter', 1),
                (7, 5, 1, 0.2, 'gallon', 1),
                (8, 1, 4, 0.05, 'kilogram', 1),
                (9, 1, 4, 50, 'gram', 1),
                (10, 2, 3, 1, 'piece', 0.5),
                (11, 7, 3, 0.1, 'pound', 1),
                (12, 7, 3, 0.1, 'ounce', 1),
                (13, 8, 6, 25, 'g', 1),
                (14, 9, 7, 100, 'ml', 1),
                (15, 10, 8, 3, 'oz', 1),
                (16, 11, 9, 15, 'fl oz', 1)
            """.trimIndent())

            // Insert comprehensive HalfProduct_Dish data
            execSQL("""
                INSERT INTO HalfProduct_Dish (halfProductDishId, halfProductId, dishId, quantity, quantityUnit) 
                VALUES 
                (1, 5, 2, 10, 'milliliter'),
                (2, 3, 2, 10, 'milliliter'),
                (3, 3, 2, 10, 'fluid ounce'),
                (4, 3, 2, 0.05, 'gallon'),
                (5, 4, 2, 0.05, 'pound'),
                (6, 4, 2, 15, 'gram'),
                (7, 4, 2, 0.15, 'kilogram'),
                (8, 1, 2, 1, 'piece'),
                (9, 1, 2, 21, 'piece'),
                (10, 6, 2, 50, 'g'),
                (11, 7, 2, 200, 'ml'),
                (12, 8, 2, 5, 'oz'),
                (13, 9, 2, 25, 'fl oz')
            """.trimIndent())

            close()
        }

        // Migrate the database to version 6
        val db = helper.runMigrationsAndValidate(TEST_DB, 6, true, Migration_5to6_UnitEnumMigration)

        // Verify the migration results
        db.apply {
            // Test products table unit conversion - comprehensive coverage
            val productsCursor = query("SELECT productId, unit, product_name FROM products ORDER BY productId")
            val expectedProductUnits = mapOf(
                1 to MeasurementUnit.KILOGRAM.name,     // per kilogram
                2 to MeasurementUnit.PIECE.name,        // per piece
                3 to MeasurementUnit.PIECE.name,        // per piece
                4 to MeasurementUnit.KILOGRAM.name,     // per kilogram
                5 to MeasurementUnit.LITER.name,        // per liter
                6 to MeasurementUnit.GALLON.name,       // per gallon
                7 to MeasurementUnit.POUND.name,        // per pound
                8 to MeasurementUnit.GRAM.name,         // g
                9 to MeasurementUnit.MILLILITER.name,   // ml
                10 to MeasurementUnit.OUNCE.name,       // oz
                11 to MeasurementUnit.FLUID_OUNCE.name, // fl oz
                12 to MeasurementUnit.PIECE.name        // pcs
            )

            while (productsCursor.moveToNext()) {
                val productId = productsCursor.getInt(0)
                val unit = productsCursor.getString(1)
                val productName = productsCursor.getString(2)
                val expectedUnit = expectedProductUnits[productId]

                assert(unit == expectedUnit) {
                    "Product $productId ($productName): expected unit $expectedUnit but got $unit"
                }
            }
            productsCursor.close()

            // Test HalfProduct table unit conversion - comprehensive coverage
            val halfProductsCursor = query("SELECT halfProductId, halfProductUnit, name FROM HalfProduct ORDER BY halfProductId")
            val expectedHalfProductUnits = mapOf(
                1 to MeasurementUnit.PIECE.name,        // per piece
                2 to MeasurementUnit.KILOGRAM.name,     // per kilogram
                3 to MeasurementUnit.LITER.name,        // per liter
                4 to MeasurementUnit.POUND.name,        // per pound
                5 to MeasurementUnit.GALLON.name,       // per gallon
                6 to MeasurementUnit.GRAM.name,         // g
                7 to MeasurementUnit.MILLILITER.name,   // ml
                8 to MeasurementUnit.OUNCE.name,        // oz
                9 to MeasurementUnit.FLUID_OUNCE.name   // fl oz
            )

            while (halfProductsCursor.moveToNext()) {
                val halfProductId = halfProductsCursor.getInt(0)
                val unit = halfProductsCursor.getString(1)
                val name = halfProductsCursor.getString(2)
                val expectedUnit = expectedHalfProductUnits[halfProductId]

                assert(unit == expectedUnit) {
                    "HalfProduct $halfProductId ($name): expected unit $expectedUnit but got $unit"
                }
            }
            halfProductsCursor.close()

            // Test Product_Dish table unit conversion - comprehensive coverage
            val productDishCursor = query("SELECT productDishId, quantityUnit, quantity FROM Product_Dish ORDER BY productDishId")
            val expectedProductDishUnits = mapOf(
                1 to MeasurementUnit.GRAM.name,         // gram
                2 to MeasurementUnit.PIECE.name,        // piece
                3 to MeasurementUnit.PIECE.name,        // piece
                4 to MeasurementUnit.GRAM.name,         // gram
                5 to MeasurementUnit.GRAM.name,         // gram
                6 to MeasurementUnit.OUNCE.name,        // ounce
                7 to MeasurementUnit.OUNCE.name,        // ounce
                8 to MeasurementUnit.OUNCE.name,        // ounce
                9 to MeasurementUnit.FLUID_OUNCE.name,  // fluid ounce
                10 to MeasurementUnit.GALLON.name,      // gallon
                11 to MeasurementUnit.MILLILITER.name,  // milliliter
                12 to MeasurementUnit.GRAM.name,        // g
                13 to MeasurementUnit.MILLILITER.name,  // ml
                14 to MeasurementUnit.OUNCE.name,       // oz
                15 to MeasurementUnit.FLUID_OUNCE.name, // fl oz
                16 to MeasurementUnit.PIECE.name        // pcs
            )

            while (productDishCursor.moveToNext()) {
                val productDishId = productDishCursor.getInt(0)
                val unit = productDishCursor.getString(1)
                val quantity = productDishCursor.getDouble(2)
                val expectedUnit = expectedProductDishUnits[productDishId]

                assert(unit == expectedUnit) {
                    "Product_Dish $productDishId (qty: $quantity): expected unit $expectedUnit but got $unit"
                }
            }
            productDishCursor.close()

            // Test Product_HalfProduct table unit conversion - comprehensive coverage
            val productHalfProductCursor = query("SELECT productHalfProductId, quantityUnit, quantity FROM Product_HalfProduct ORDER BY productHalfProductId")
            val expectedProductHalfProductUnits = mapOf(
                1 to MeasurementUnit.KILOGRAM.name,     // kilogram
                2 to MeasurementUnit.POUND.name,        // pound
                3 to MeasurementUnit.GRAM.name,         // gram
                4 to MeasurementUnit.GRAM.name,         // gram
                5 to MeasurementUnit.FLUID_OUNCE.name,  // fluid ounce
                6 to MeasurementUnit.MILLILITER.name,   // milliliter
                7 to MeasurementUnit.GALLON.name,       // gallon
                8 to MeasurementUnit.KILOGRAM.name,     // kilogram
                9 to MeasurementUnit.GRAM.name,         // gram
                10 to MeasurementUnit.PIECE.name,       // piece
                11 to MeasurementUnit.POUND.name,       // pound
                12 to MeasurementUnit.OUNCE.name,       // ounce
                13 to MeasurementUnit.GRAM.name,        // g
                14 to MeasurementUnit.MILLILITER.name,  // ml
                15 to MeasurementUnit.OUNCE.name,       // oz
                16 to MeasurementUnit.FLUID_OUNCE.name  // fl oz
            )

            while (productHalfProductCursor.moveToNext()) {
                val productHalfProductId = productHalfProductCursor.getInt(0)
                val unit = productHalfProductCursor.getString(1)
                val quantity = productHalfProductCursor.getDouble(2)
                val expectedUnit = expectedProductHalfProductUnits[productHalfProductId]

                assert(unit == expectedUnit) {
                    "Product_HalfProduct $productHalfProductId (qty: $quantity): expected unit $expectedUnit but got $unit"
                }
            }
            productHalfProductCursor.close()

            // Test HalfProduct_Dish table unit conversion - comprehensive coverage
            val halfProductDishCursor = query("SELECT halfProductDishId, quantityUnit, quantity FROM HalfProduct_Dish ORDER BY halfProductDishId")
            val expectedHalfProductDishUnits = mapOf(
                1 to MeasurementUnit.MILLILITER.name,   // milliliter
                2 to MeasurementUnit.MILLILITER.name,   // milliliter
                3 to MeasurementUnit.FLUID_OUNCE.name,  // fluid ounce
                4 to MeasurementUnit.GALLON.name,       // gallon
                5 to MeasurementUnit.POUND.name,        // pound
                6 to MeasurementUnit.GRAM.name,         // gram
                7 to MeasurementUnit.KILOGRAM.name,     // kilogram
                8 to MeasurementUnit.PIECE.name,        // piece
                9 to MeasurementUnit.PIECE.name,        // piece
                10 to MeasurementUnit.GRAM.name,        // g
                11 to MeasurementUnit.MILLILITER.name,  // ml
                12 to MeasurementUnit.OUNCE.name,       // oz
                13 to MeasurementUnit.FLUID_OUNCE.name  // fl oz
            )

            while (halfProductDishCursor.moveToNext()) {
                val halfProductDishId = halfProductDishCursor.getInt(0)
                val unit = halfProductDishCursor.getString(1)
                val quantity = halfProductDishCursor.getDouble(2)
                val expectedUnit = expectedHalfProductDishUnits[halfProductDishId]

                assert(unit == expectedUnit) {
                    "HalfProduct_Dish $halfProductDishId (qty: $quantity): expected unit $expectedUnit but got $unit"
                }
            }
            halfProductDishCursor.close()

            // Verify that all data counts are preserved
            val productsCount = query("SELECT COUNT(*) FROM products").use { it.moveToFirst(); it.getInt(0) }
            assert(productsCount == 12) { "Products count mismatch - expected 12, got $productsCount" }

            val halfProductsCount = query("SELECT COUNT(*) FROM HalfProduct").use { it.moveToFirst(); it.getInt(0) }
            assert(halfProductsCount == 9) { "HalfProducts count mismatch - expected 9, got $halfProductsCount" }

            val productDishCount = query("SELECT COUNT(*) FROM Product_Dish").use { it.moveToFirst(); it.getInt(0) }
            assert(productDishCount == 16) { "Product_Dish count mismatch - expected 16, got $productDishCount" }

            val productHalfProductCount = query("SELECT COUNT(*) FROM Product_HalfProduct").use { it.moveToFirst(); it.getInt(0) }
            assert(productHalfProductCount == 16) { "Product_HalfProduct count mismatch - expected 16, got $productHalfProductCount" }

            val halfProductDishCount = query("SELECT COUNT(*) FROM HalfProduct_Dish").use { it.moveToFirst(); it.getInt(0) }
            assert(halfProductDishCount == 13) { "HalfProduct_Dish count mismatch - expected 13, got $halfProductDishCount" }

            // Verify that numeric values and names are preserved correctly
            val sampleProductCursor = query("SELECT product_name, pricePerUnit, tax, waste FROM products WHERE productId = 1")
            sampleProductCursor.moveToFirst()
            assert(sampleProductCursor.getString(0) == "Minced Beef") { "Product name not preserved" }
            assert(sampleProductCursor.getDouble(1) == 19.2) { "Product price not preserved" }
            assert(sampleProductCursor.getDouble(2) == 0.0) { "Product tax not preserved" }
            assert(sampleProductCursor.getDouble(3) == 0.0) { "Product waste not preserved" }
            sampleProductCursor.close()

            // Test edge case: verify products with tax and waste are preserved
            val taxWasteProductCursor = query("SELECT tax, waste FROM products WHERE productId = 4")
            taxWasteProductCursor.moveToFirst()
            assert(taxWasteProductCursor.getDouble(0) == 0.0) { "Tax value not preserved for product 4" }
            assert(taxWasteProductCursor.getDouble(1) == 15.0) { "Waste value not preserved for product 4" }
            taxWasteProductCursor.close()

            // Verify schema integrity - check that all tables still exist and have correct structure
            val tablesCursor = query("SELECT name FROM sqlite_master WHERE type='table' ORDER BY name")
            val expectedTables = setOf("products", "dishes", "HalfProduct", "Product_Dish", "Product_HalfProduct", "HalfProduct_Dish")
            val actualTables = mutableSetOf<String>()

            while (tablesCursor.moveToNext()) {
                val tableName = tablesCursor.getString(0)
                if (!tableName.startsWith("android_") && !tableName.startsWith("sqlite_")) {
                    actualTables.add(tableName)
                }
            }
            tablesCursor.close()

            assert(actualTables.containsAll(expectedTables)) {
                "Missing expected tables. Expected: $expectedTables, Actual: $actualTables"
            }

            close()
        }
    }

    @Test
    fun testMigration_6_to_7() {
        // Create the database with version 6 schema and insert comprehensive test data
        helper.createDatabase(TEST_DB, 6).apply {
            // Insert test products with the old schema (pricePerUnit) - comprehensive coverage
            execSQL("""
                INSERT INTO products (productId, product_name, pricePerUnit, tax, waste, unit) 
                VALUES 
                (1, 'Minced Beef', 19.2, 0.0, 0.0, 'KILOGRAM'),
                (2, 'Burger Bun', 0.7, 0.0, 0.0, 'PIECE'),
                (3, 'Cheese Slice', 0.5, 0.0, 0.0, 'PIECE'),
                (4, 'Lettuce', 3.99, 0.0, 15.0, 'KILOGRAM'),
                (5, 'Milk', 1.0, 0.0, 0.0, 'LITER'),
                (6, 'Oil', 10.0, 0.0, 0.0, 'GALLON'),
                (7, 'Flour', 1.0, 0.0, 0.0, 'POUND'),
                (8, 'Premium Beef', 25.5, 5.0, 2.5, 'KILOGRAM'),
                (9, 'Artisan Bread', 3.2, 8.0, 1.0, 'PIECE'),
                (10, 'Organic Milk', 2.8, 3.5, 0.0, 'LITER'),
                (11, 'Specialty Oil', 15.75, 10.0, 0.5, 'GALLON'),
                (12, 'Whole Wheat Flour', 2.1, 2.0, 3.0, 'POUND'),
                (13, 'Test Product Gram', 5.25, 4.5, 1.5, 'GRAM'),
                (14, 'Test Product ML', 0.85, 6.0, 0.0, 'MILLILITER'),
                (15, 'Test Product Ounce', 12.0, 0.0, 8.0, 'OUNCE'),
                (16, 'Test Product Fluid Ounce', 4.75, 7.5, 2.0, 'FLUID_OUNCE')
            """.trimIndent())

            close()
        }

        // Migrate the database to version 7
        val db = helper.runMigrationsAndValidate(TEST_DB, 7, true, Migration_6to7_ProductBaseSchema)

        // Verify the migration results
        db.apply {
            // Test that all data is preserved and new fields are correctly set - comprehensive verification
            val productsCursor = query("SELECT productId, product_name, input_method, package_price, package_quantity, package_unit, canonical_price, canonical_unit, tax, waste FROM products ORDER BY productId")

            val expectedProducts = listOf(
                // productId, name, canonical_price, tax, waste, canonical_unit
                listOf(1L, "Minced Beef", 19.2, 0.0, 0.0, "KILOGRAM"),
                listOf(2L, "Burger Bun", 0.7, 0.0, 0.0, "PIECE"),
                listOf(3L, "Cheese Slice", 0.5, 0.0, 0.0, "PIECE"),
                listOf(4L, "Lettuce", 3.99, 0.0, 15.0, "KILOGRAM"),
                listOf(5L, "Milk", 1.0, 0.0, 0.0, "LITER"),
                listOf(6L, "Oil", 10.0, 0.0, 0.0, "GALLON"),
                listOf(7L, "Flour", 1.0, 0.0, 0.0, "POUND"),
                listOf(8L, "Premium Beef", 25.5, 5.0, 2.5, "KILOGRAM"),
                listOf(9L, "Artisan Bread", 3.2, 8.0, 1.0, "PIECE"),
                listOf(10L, "Organic Milk", 2.8, 3.5, 0.0, "LITER"),
                listOf(11L, "Specialty Oil", 15.75, 10.0, 0.5, "GALLON"),
                listOf(12L, "Whole Wheat Flour", 2.1, 2.0, 3.0, "POUND"),
                listOf(13L, "Test Product Gram", 5.25, 4.5, 1.5, "GRAM"),
                listOf(14L, "Test Product ML", 0.85, 6.0, 0.0, "MILLILITER"),
                listOf(15L, "Test Product Ounce", 12.0, 0.0, 8.0, "OUNCE"),
                listOf(16L, "Test Product Fluid Ounce", 4.75, 7.5, 2.0, "FLUID_OUNCE")
            )

            var index = 0
            while (productsCursor.moveToNext()) {
                val productId = productsCursor.getLong(0)
                val productName = productsCursor.getString(1)
                val inputMethod = productsCursor.getString(2)
                val packagePrice = if (productsCursor.isNull(3)) null else productsCursor.getDouble(3)
                val packageQuantity = if (productsCursor.isNull(4)) null else productsCursor.getDouble(4)
                val packageUnit = if (productsCursor.isNull(5)) null else productsCursor.getString(5)
                val canonicalPrice = productsCursor.getDouble(6)
                val canonicalUnit = productsCursor.getString(7)
                val tax = productsCursor.getDouble(8)
                val waste = productsCursor.getDouble(9)

                val expected = expectedProducts[index]

                // Verify data integrity with detailed assertions
                assert(productId == expected[0] as Long) {
                    "Product ID mismatch at index $index: expected ${expected[0]}, got $productId"
                }
                assert(productName == expected[1] as String) {
                    "Product name mismatch at index $index: expected ${expected[1]}, got $productName"
                }
                assert(canonicalPrice == expected[2] as Double) {
                    "Canonical price mismatch for product $productId: expected ${expected[2]}, got $canonicalPrice"
                }
                assert(tax == expected[3] as Double) {
                    "Tax mismatch for product $productId: expected ${expected[3]}, got $tax"
                }
                assert(waste == expected[4] as Double) {
                    "Waste mismatch for product $productId: expected ${expected[4]}, got $waste"
                }
                assert(canonicalUnit == expected[5] as String) {
                    "Canonical unit mismatch for product $productId: expected ${expected[5]}, got $canonicalUnit"
                }

                // Verify new fields are properly set
                assert(inputMethod == "UNIT") {
                    "Input method should default to UNIT for product $productId, got $inputMethod"
                }
                assert(packagePrice == null) {
                    "Package price should be null for migrated product $productId, got $packagePrice"
                }
                assert(packageQuantity == null) {
                    "Package quantity should be null for migrated product $productId, got $packageQuantity"
                }
                assert(packageUnit == null) {
                    "Package unit should be null for migrated product $productId, got $packageUnit"
                }

                index++
            }
            productsCursor.close()

            // Verify that all products were migrated
            assert(index == 16) { "Expected 16 products, but got $index" }

            // Verify data count is preserved
            val countCursor = query("SELECT COUNT(*) FROM products")
            countCursor.moveToFirst()
            assert(countCursor.getInt(0) == 16) { "Products count mismatch - expected 16, got ${countCursor.getInt(0)}" }
            countCursor.close()

            // Test specific edge cases and value preservation

            // Test product with highest tax and waste values
            val highTaxWasteCursor = query("SELECT tax, waste, canonical_price FROM products WHERE productId = 11")
            highTaxWasteCursor.moveToFirst()
            assert(highTaxWasteCursor.getDouble(0) == 10.0) { "Highest tax value not preserved correctly" }
            assert(highTaxWasteCursor.getDouble(1) == 0.5) { "Waste value for high-tax product not preserved" }
            assert(highTaxWasteCursor.getDouble(2) == 15.75) { "Price for high-tax product not preserved" }
            highTaxWasteCursor.close()

            // Test product with highest waste value
            val highWasteCursor = query("SELECT waste, tax, canonical_price FROM products WHERE productId = 4")
            highWasteCursor.moveToFirst()
            assert(highWasteCursor.getDouble(0) == 15.0) { "Highest waste value not preserved correctly" }
            assert(highWasteCursor.getDouble(1) == 0.0) { "Tax for high-waste product not preserved" }
            assert(highWasteCursor.getDouble(2) == 3.99) { "Price for high-waste product not preserved" }
            highWasteCursor.close()

            // Test decimal precision preservation
            val decimalPrecisionCursor = query("SELECT canonical_price FROM products WHERE productId = 8")
            decimalPrecisionCursor.moveToFirst()
            assert(decimalPrecisionCursor.getDouble(0) == 25.5) { "Decimal precision not preserved for price 25.5" }
            decimalPrecisionCursor.close()

            // Test various unit types are preserved correctly
            val unitTypesCursor = query("SELECT productId, canonical_unit FROM products WHERE productId IN (13, 14, 15, 16) ORDER BY productId")
            val expectedUnits = listOf("GRAM", "MILLILITER", "OUNCE", "FLUID_OUNCE")
            var unitIndex = 0
            while (unitTypesCursor.moveToNext()) {
                val unit = unitTypesCursor.getString(1)
                val expectedUnit = expectedUnits[unitIndex]
                assert(unit == expectedUnit) {
                    "Unit type mismatch for additional product: expected $expectedUnit, got $unit"
                }
                unitIndex++
            }
            unitTypesCursor.close()

            // Verify table structure - check that the old columns don't exist and new columns do exist
            val pragmaCursor = query("PRAGMA table_info(products)")
            val columns = mutableSetOf<String>()
            val columnDetails = mutableMapOf<String, String>()

            while (pragmaCursor.moveToNext()) {
                val columnName = pragmaCursor.getString(1) // Column name is at index 1
                val columnType = pragmaCursor.getString(2) // Column type is at index 2
                val notNull = pragmaCursor.getInt(3) == 1  // Not null constraint is at index 3
                val defaultValue = pragmaCursor.getString(4) // Default value is at index 4

                columns.add(columnName)
                columnDetails[columnName] = "$columnType${if (notNull) " NOT NULL" else ""}${if (defaultValue != null) " DEFAULT $defaultValue" else ""}"
            }
            pragmaCursor.close()

            // Verify new schema structure
            val expectedColumns = setOf(
                "productId", "product_name", "input_method", "package_price",
                "package_quantity", "package_unit", "canonical_price", "canonical_unit",
                "tax", "waste"
            )

            assert(columns.containsAll(expectedColumns)) {
                "Missing expected columns. Expected: $expectedColumns, Found: $columns"
            }

            // Verify specific column constraints
            assert(columnDetails["input_method"]?.contains("NOT NULL") == true) {
                "input_method should be NOT NULL, got: ${columnDetails["input_method"]}"
            }
            assert(columnDetails["input_method"]?.contains("DEFAULT 'UNIT'") == true) {
                "input_method should have DEFAULT 'UNIT', got: ${columnDetails["input_method"]}"
            }
            assert(columnDetails["canonical_price"]?.contains("NOT NULL") == true) {
                "canonical_price should be NOT NULL, got: ${columnDetails["canonical_price"]}"
            }
            assert(columnDetails["canonical_unit"]?.contains("NOT NULL") == true) {
                "canonical_unit should be NOT NULL, got: ${columnDetails["canonical_unit"]}"
            }

            // Verify old columns are completely removed
            assert(!columns.contains("pricePerUnit")) {
                "Old column 'pricePerUnit' should not exist after migration, but found in: $columns"
            }
            assert(!columns.contains("unit")) {
                "Old column 'unit' should not exist after migration (renamed to canonical_unit), but found in: $columns"
            }

            // Verify that the exact column count matches expectations (10 columns)
            assert(columns.size == 10) {
                "Expected exactly 10 columns, but found ${columns.size}: $columns"
            }

            // Test data integrity for products with zero values
            val zeroValuesCursor = query("SELECT tax, waste FROM products WHERE productId IN (1, 2, 3, 5, 7) ORDER BY productId")
            while (zeroValuesCursor.moveToNext()) {
                val tax = zeroValuesCursor.getDouble(0)
                val waste = zeroValuesCursor.getDouble(1)
                // These products should have zero tax and waste
                assert(tax == 0.0) { "Zero tax value not preserved correctly" }
                assert(waste == 0.0) { "Zero waste value not preserved correctly" }
            }
            zeroValuesCursor.close()

            close()
        }
    }

    // Migration 5→6 Tests - Split into focused test cases

    @Test
    fun testMigration_5_to_6_productUnits() {
        // Create the database with version 5 schema and insert product data
        helper.createDatabase(TEST_DB, 5).apply {
            execSQL("""
                INSERT INTO products (productId, product_name, pricePerUnit, tax, waste, unit) 
                VALUES 
                (1, 'Minced Beef', 19.2, 0, 0, 'per kilogram'),
                (2, 'Burger Bun', 0.7, 0, 0, 'per piece'),
                (3, 'Cheese Slice', 0.5, 0, 0, 'per piece'),
                (4, 'Lettuce', 3.99, 0, 15, 'per kilogram'),
                (5, 'Milk', 1, 0, 0, 'per liter'),
                (6, 'Oil', 10, 0, 0, 'per gallon'),
                (7, 'Flour', 1, 0, 0, 'per pound'),
                (8, 'Test Gram Product', 5.0, 2.5, 0, 'g'),
                (9, 'Test ML Product', 3.0, 1.0, 5, 'ml'),
                (10, 'Test Ounce Product', 8.0, 0, 10, 'oz'),
                (11, 'Test Fluid Ounce Product', 4.5, 3.0, 0, 'fl oz'),
                (12, 'Test Piece Product', 1.25, 5.0, 2, 'pcs')
            """.trimIndent())
            close()
        }

        // Migrate and verify products table unit conversion
        val db = helper.runMigrationsAndValidate(TEST_DB, 6, true, Migration_5to6_UnitEnumMigration)

        db.apply {
            val productsCursor = query("SELECT productId, unit, product_name FROM products ORDER BY productId")
            val expectedProductUnits = mapOf(
                1 to MeasurementUnit.KILOGRAM.name,     // per kilogram
                2 to MeasurementUnit.PIECE.name,        // per piece
                3 to MeasurementUnit.PIECE.name,        // per piece
                4 to MeasurementUnit.KILOGRAM.name,     // per kilogram
                5 to MeasurementUnit.LITER.name,        // per liter
                6 to MeasurementUnit.GALLON.name,       // per gallon
                7 to MeasurementUnit.POUND.name,        // per pound
                8 to MeasurementUnit.GRAM.name,         // g
                9 to MeasurementUnit.MILLILITER.name,   // ml
                10 to MeasurementUnit.OUNCE.name,       // oz
                11 to MeasurementUnit.FLUID_OUNCE.name, // fl oz
                12 to MeasurementUnit.PIECE.name        // pcs
            )

            while (productsCursor.moveToNext()) {
                val productId = productsCursor.getInt(0)
                val unit = productsCursor.getString(1)
                val productName = productsCursor.getString(2)
                val expectedUnit = expectedProductUnits[productId]

                assert(unit == expectedUnit) {
                    "Product $productId ($productName): expected unit $expectedUnit but got $unit"
                }
            }
            productsCursor.close()

            // Verify data preservation
            val sampleProductCursor = query("SELECT product_name, pricePerUnit, tax, waste FROM products WHERE productId = 1")
            sampleProductCursor.moveToFirst()
            assert(sampleProductCursor.getString(0) == "Minced Beef") { "Product name not preserved" }
            assert(sampleProductCursor.getDouble(1) == 19.2) { "Product price not preserved" }
            assert(sampleProductCursor.getDouble(2) == 0.0) { "Product tax not preserved" }
            assert(sampleProductCursor.getDouble(3) == 0.0) { "Product waste not preserved" }
            sampleProductCursor.close()

            close()
        }
    }

    @Test
    fun testMigration_5_to_6_halfProductUnits() {
        // Create the database with version 5 schema and insert HalfProduct data
        helper.createDatabase(TEST_DB, 5).apply {
            execSQL("""
                INSERT INTO HalfProduct (halfProductId, name, halfProductUnit) 
                VALUES 
                (1, 'Piece Mix', 'per piece'),
                (2, 'Kg Mix', 'per kilogram'),
                (3, 'Liter Mix', 'per liter'),
                (4, 'Pound Mix', 'per pound'),
                (5, 'Gallon Mix', 'per gallon'),
                (6, 'Gram Mix', 'g'),
                (7, 'ML Mix', 'ml'),
                (8, 'Ounce Mix', 'oz'),
                (9, 'Fluid Ounce Mix', 'fl oz')
            """.trimIndent())
            close()
        }

        // Migrate and verify HalfProduct table unit conversion
        val db = helper.runMigrationsAndValidate(TEST_DB, 6, true, Migration_5to6_UnitEnumMigration)

        db.apply {
            val halfProductsCursor = query("SELECT halfProductId, halfProductUnit, name FROM HalfProduct ORDER BY halfProductId")
            val expectedHalfProductUnits = mapOf(
                1 to MeasurementUnit.PIECE.name,        // per piece
                2 to MeasurementUnit.KILOGRAM.name,     // per kilogram
                3 to MeasurementUnit.LITER.name,        // per liter
                4 to MeasurementUnit.POUND.name,        // per pound
                5 to MeasurementUnit.GALLON.name,       // per gallon
                6 to MeasurementUnit.GRAM.name,         // g
                7 to MeasurementUnit.MILLILITER.name,   // ml
                8 to MeasurementUnit.OUNCE.name,        // oz
                9 to MeasurementUnit.FLUID_OUNCE.name   // fl oz
            )

            while (halfProductsCursor.moveToNext()) {
                val halfProductId = halfProductsCursor.getInt(0)
                val unit = halfProductsCursor.getString(1)
                val name = halfProductsCursor.getString(2)
                val expectedUnit = expectedHalfProductUnits[halfProductId]

                assert(unit == expectedUnit) {
                    "HalfProduct $halfProductId ($name): expected unit $expectedUnit but got $unit"
                }
            }
            halfProductsCursor.close()
            close()
        }
    }

    @Test
    fun testMigration_5_to_6_relationshipTables() {
        // Create the database with version 5 schema and insert relationship data
        helper.createDatabase(TEST_DB, 5).apply {
            // Insert basic required data
            execSQL("""
                INSERT INTO products (productId, product_name, pricePerUnit, tax, waste, unit) 
                VALUES (1, 'Test Product', 10.0, 0, 0, 'per kilogram')
            """.trimIndent())

            execSQL("""
                INSERT INTO dishes (dishId, dish_name, margin_percent, dish_tax) 
                VALUES (1, 'Test Dish', 100.0, 0.0)
            """.trimIndent())

            execSQL("""
                INSERT INTO HalfProduct (halfProductId, name, halfProductUnit) 
                VALUES (1, 'Test Half Product', 'per piece')
            """.trimIndent())

            // Insert relationship data with various unit types
            execSQL("""
                INSERT INTO Product_Dish (productDishId, productId, dishId, quantity, quantityUnit) 
                VALUES 
                (1, 1, 1, 150, 'gram'),
                (2, 1, 1, 1, 'ounce'),
                (3, 1, 1, 20, 'milliliter'),
                (4, 1, 1, 0.1, 'gallon'),
                (5, 1, 1, 5, 'pcs')
            """.trimIndent())

            execSQL("""
                INSERT INTO Product_HalfProduct (productHalfProductId, productId, halfProductId, quantity, quantityUnit, weightPiece) 
                VALUES 
                (1, 1, 1, 1, 'kilogram', 1),
                (2, 1, 1, 50, 'gram', 1),
                (3, 1, 1, 2, 'fluid ounce', 1),
                (4, 1, 1, 0.2, 'gallon', 1)
            """.trimIndent())

            execSQL("""
                INSERT INTO HalfProduct_Dish (halfProductDishId, halfProductId, dishId, quantity, quantityUnit) 
                VALUES 
                (1, 1, 1, 10, 'milliliter'),
                (2, 1, 1, 15, 'gram'),
                (3, 1, 1, 0.15, 'kilogram'),
                (4, 1, 1, 1, 'piece')
            """.trimIndent())

            close()
        }

        // Migrate and verify relationship tables unit conversion
        val db = helper.runMigrationsAndValidate(TEST_DB, 6, true, Migration_5to6_UnitEnumMigration)

        db.apply {
            // Test Product_Dish conversion
            val productDishCursor = query("SELECT productDishId, quantityUnit FROM Product_Dish ORDER BY productDishId")
            val expectedProductDishUnits = listOf(
                MeasurementUnit.GRAM.name,         // gram
                MeasurementUnit.OUNCE.name,        // ounce
                MeasurementUnit.MILLILITER.name,   // milliliter
                MeasurementUnit.GALLON.name,       // gallon
                MeasurementUnit.PIECE.name         // pcs
            )

            var index = 0
            while (productDishCursor.moveToNext()) {
                val unit = productDishCursor.getString(1)
                assert(unit == expectedProductDishUnits[index]) {
                    "Product_Dish row ${index + 1}: expected ${expectedProductDishUnits[index]} but got $unit"
                }
                index++
            }
            productDishCursor.close()

            // Test Product_HalfProduct conversion
            val productHalfProductCursor = query("SELECT productHalfProductId, quantityUnit FROM Product_HalfProduct ORDER BY productHalfProductId")
            val expectedProductHalfProductUnits = listOf(
                MeasurementUnit.KILOGRAM.name,     // kilogram
                MeasurementUnit.GRAM.name,         // gram
                MeasurementUnit.FLUID_OUNCE.name,  // fluid ounce
                MeasurementUnit.GALLON.name        // gallon
            )

            index = 0
            while (productHalfProductCursor.moveToNext()) {
                val unit = productHalfProductCursor.getString(1)
                assert(unit == expectedProductHalfProductUnits[index]) {
                    "Product_HalfProduct row ${index + 1}: expected ${expectedProductHalfProductUnits[index]} but got $unit"
                }
                index++
            }
            productHalfProductCursor.close()

            // Test HalfProduct_Dish conversion
            val halfProductDishCursor = query("SELECT halfProductDishId, quantityUnit FROM HalfProduct_Dish ORDER BY halfProductDishId")
            val expectedHalfProductDishUnits = listOf(
                MeasurementUnit.MILLILITER.name,   // milliliter
                MeasurementUnit.GRAM.name,         // gram
                MeasurementUnit.KILOGRAM.name,     // kilogram
                MeasurementUnit.PIECE.name         // piece
            )

            index = 0
            while (halfProductDishCursor.moveToNext()) {
                val unit = halfProductDishCursor.getString(1)
                assert(unit == expectedHalfProductDishUnits[index]) {
                    "HalfProduct_Dish row ${index + 1}: expected ${expectedHalfProductDishUnits[index]} but got $unit"
                }
                index++
            }
            halfProductDishCursor.close()

            close()
        }
    }

    @Test
    fun testMigration_5_to_6_schemaIntegrity() {
        // Create the database with version 5 schema and minimal data
        helper.createDatabase(TEST_DB, 5).apply {
            execSQL("INSERT INTO products (productId, product_name, pricePerUnit, tax, waste, unit) VALUES (1, 'Test', 1.0, 0, 0, 'per kilogram')")
            execSQL("INSERT INTO HalfProduct (halfProductId, name, halfProductUnit) VALUES (1, 'Test', 'per piece')")
            execSQL("INSERT INTO dishes (dishId, dish_name, margin_percent, dish_tax) VALUES (1, 'Test', 100.0, 0.0)")
            close()
        }

        // Migrate and verify schema integrity
        val db = helper.runMigrationsAndValidate(TEST_DB, 6, true, Migration_5to6_UnitEnumMigration)

        db.apply {
            // Verify all expected tables exist
            val tablesCursor = query("SELECT name FROM sqlite_master WHERE type='table' ORDER BY name")
            val expectedTables = setOf("products", "dishes", "HalfProduct", "Product_Dish", "Product_HalfProduct", "HalfProduct_Dish")
            val actualTables = mutableSetOf<String>()

            while (tablesCursor.moveToNext()) {
                val tableName = tablesCursor.getString(0)
                if (!tableName.startsWith("android_") && !tableName.startsWith("sqlite_")) {
                    actualTables.add(tableName)
                }
            }
            tablesCursor.close()

            assert(actualTables.containsAll(expectedTables)) {
                "Missing expected tables. Expected: $expectedTables, Actual: $actualTables"
            }

            // Verify indexes were recreated
            val indexesCursor = query("SELECT name FROM sqlite_master WHERE type='index' AND name LIKE 'index_%'")
            val indexNames = mutableSetOf<String>()
            while (indexesCursor.moveToNext()) {
                indexNames.add(indexesCursor.getString(0))
            }
            indexesCursor.close()

            val expectedIndexes = setOf(
                "index_Product_Dish_productId", "index_Product_Dish_dishId",
                "index_Product_HalfProduct_productId", "index_Product_HalfProduct_halfProductId",
                "index_HalfProduct_Dish_halfProductId", "index_HalfProduct_Dish_dishId"
            )

            assert(indexNames.containsAll(expectedIndexes)) {
                "Missing expected indexes. Expected: $expectedIndexes, Found: $indexNames"
            }

            close()
        }
    }

    // Migration 6→7 Tests - Split into focused test cases

    @Test
    fun testMigration_6_to_7_schemaTransformation() {
        // Create the database with version 6 schema
        helper.createDatabase(TEST_DB, 6).apply {
            execSQL("""
                INSERT INTO products (productId, product_name, pricePerUnit, tax, waste, unit) 
                VALUES 
                (1, 'Minced Beef', 19.2, 0.0, 0.0, 'KILOGRAM'),
                (2, 'Burger Bun', 0.7, 5.0, 2.5, 'PIECE'),
                (3, 'Test Product', 12.45, 3.5, 1.5, 'LITER')
            """.trimIndent())
            close()
        }

        // Migrate and verify schema transformation
        val db = helper.runMigrationsAndValidate(TEST_DB, 7, true, Migration_6to7_ProductBaseSchema)

        db.apply {
            // Verify table structure
            val pragmaCursor = query("PRAGMA table_info(products)")
            val columns = mutableSetOf<String>()
            val columnDetails = mutableMapOf<String, String>()

            while (pragmaCursor.moveToNext()) {
                val columnName = pragmaCursor.getString(1)
                val columnType = pragmaCursor.getString(2)
                val notNull = pragmaCursor.getInt(3) == 1
                val defaultValue = pragmaCursor.getString(4)

                columns.add(columnName)
                columnDetails[columnName] = "$columnType${if (notNull) " NOT NULL" else ""}${if (defaultValue != null) " DEFAULT $defaultValue" else ""}"
            }
            pragmaCursor.close()

            // Verify new schema structure
            val expectedColumns = setOf(
                "productId", "product_name", "input_method", "package_price",
                "package_quantity", "package_unit", "canonical_price", "canonical_unit",
                "tax", "waste"
            )

            assert(columns.containsAll(expectedColumns)) {
                "Missing expected columns. Expected: $expectedColumns, Found: $columns"
            }

            // Verify old columns are removed
            assert(!columns.contains("pricePerUnit")) {
                "Old column 'pricePerUnit' should not exist after migration"
            }
            assert(!columns.contains("unit")) {
                "Old column 'unit' should not exist after migration"
            }

            // Verify constraints
            assert(columnDetails["input_method"]?.contains("NOT NULL") == true) {
                "input_method should be NOT NULL"
            }
            assert(columnDetails["input_method"]?.contains("DEFAULT 'UNIT'") == true) {
                "input_method should have DEFAULT 'UNIT'"
            }

            close()
        }
    }

    @Test
    fun testMigration_6_to_7_dataPreservation() {
        // Create the database with version 6 schema and varied data
        helper.createDatabase(TEST_DB, 6).apply {
            execSQL("""
                INSERT INTO products (productId, product_name, pricePerUnit, tax, waste, unit) 
                VALUES 
                (1, 'Minced Beef', 19.2, 0.0, 0.0, 'KILOGRAM'),
                (2, 'Burger Bun', 0.7, 5.0, 2.5, 'PIECE'),
                (3, 'Premium Oil', 15.75, 10.0, 0.5, 'GALLON'),
                (4, 'Lettuce', 3.99, 0.0, 15.0, 'KILOGRAM'),
                (5, 'Test Precision', 25.5, 7.5, 8.0, 'OUNCE')
            """.trimIndent())
            close()
        }

        // Migrate and verify data preservation
        val db = helper.runMigrationsAndValidate(TEST_DB, 7, true, Migration_6to7_ProductBaseSchema)

        db.apply {
            val productsCursor = query("SELECT productId, product_name, canonical_price, canonical_unit, tax, waste, input_method, package_price FROM products ORDER BY productId")

            val expectedData = listOf(
                listOf(1L, "Minced Beef", 19.2, "KILOGRAM", 0.0, 0.0),
                listOf(2L, "Burger Bun", 0.7, "PIECE", 5.0, 2.5),
                listOf(3L, "Premium Oil", 15.75, "GALLON", 10.0, 0.5),
                listOf(4L, "Lettuce", 3.99, "KILOGRAM", 0.0, 15.0),
                listOf(5L, "Test Precision", 25.5, "OUNCE", 7.5, 8.0)
            )

            var index = 0
            while (productsCursor.moveToNext()) {
                val expected = expectedData[index]

                assert(productsCursor.getLong(0) == expected[0] as Long) { "Product ID mismatch at index $index" }
                assert(productsCursor.getString(1) == expected[1] as String) { "Product name mismatch at index $index" }
                assert(productsCursor.getDouble(2) == expected[2] as Double) { "Canonical price mismatch at index $index" }
                assert(productsCursor.getString(3) == expected[3] as String) { "Canonical unit mismatch at index $index" }
                assert(productsCursor.getDouble(4) == expected[4] as Double) { "Tax mismatch at index $index" }
                assert(productsCursor.getDouble(5) == expected[5] as Double) { "Waste mismatch at index $index" }

                // Verify new fields
                assert(productsCursor.getString(6) == "UNIT") { "Input method should default to UNIT" }
                assert(productsCursor.isNull(7)) { "Package price should be null" }

                index++
            }
            productsCursor.close()

            close()
        }
    }

    @Test
    fun testMigration_6_to_7_edgeCases() {
        // Create the database with version 6 schema and edge case data
        helper.createDatabase(TEST_DB, 6).apply {
            execSQL("""
                INSERT INTO products (productId, product_name, pricePerUnit, tax, waste, unit) 
                VALUES 
                (1, 'Zero Values', 0.0, 0.0, 0.0, 'GRAM'),
                (2, 'High Values', 999.99, 99.9, 99.9, 'FLUID_OUNCE'),
                (3, 'Decimal Precision', 12.345, 1.23, 4.56, 'MILLILITER'),
                (4, 'Special Chars éñ', 5.0, 2.5, 0.0, 'POUND')
            """.trimIndent())
            close()
        }

        // Migrate and verify edge cases
        val db = helper.runMigrationsAndValidate(TEST_DB, 7, true, Migration_6to7_ProductBaseSchema)

        db.apply {
            // Test zero values preservation
            val zeroValuesCursor = query("SELECT canonical_price, tax, waste FROM products WHERE productId = 1")
            zeroValuesCursor.moveToFirst()
            assert(zeroValuesCursor.getDouble(0) == 0.0) { "Zero price not preserved" }
            assert(zeroValuesCursor.getDouble(1) == 0.0) { "Zero tax not preserved" }
            assert(zeroValuesCursor.getDouble(2) == 0.0) { "Zero waste not preserved" }
            zeroValuesCursor.close()

            // Test high values preservation
            val highValuesCursor = query("SELECT canonical_price, tax, waste FROM products WHERE productId = 2")
            highValuesCursor.moveToFirst()
            assert(highValuesCursor.getDouble(0) == 999.99) { "High price not preserved" }
            assert(highValuesCursor.getDouble(1) == 99.9) { "High tax not preserved" }
            assert(highValuesCursor.getDouble(2) == 99.9) { "High waste not preserved" }
            highValuesCursor.close()

            // Test decimal precision
            val precisionCursor = query("SELECT canonical_price, tax, waste FROM products WHERE productId = 3")
            precisionCursor.moveToFirst()
            assert(precisionCursor.getDouble(0) == 12.345) { "Decimal precision not preserved for price" }
            assert(precisionCursor.getDouble(1) == 1.23) { "Decimal precision not preserved for tax" }
            assert(precisionCursor.getDouble(2) == 4.56) { "Decimal precision not preserved for waste" }
            precisionCursor.close()

            // Test special characters in product name
            val specialCharsCursor = query("SELECT product_name FROM products WHERE productId = 4")
            specialCharsCursor.moveToFirst()
            assert(specialCharsCursor.getString(0) == "Special Chars éñ") { "Special characters not preserved" }
            specialCharsCursor.close()

            close()
        }
    }

    @Test
    fun testMigration_6_to_7_newFieldDefaults() {
        // Create the database with version 6 schema
        helper.createDatabase(TEST_DB, 6).apply {
            execSQL("""
                INSERT INTO products (productId, product_name, pricePerUnit, tax, waste, unit) 
                VALUES (1, 'Test Product', 10.0, 5.0, 2.0, 'KILOGRAM')
            """.trimIndent())
            close()
        }

        // Migrate and verify new field defaults
        val db = helper.runMigrationsAndValidate(TEST_DB, 7, true, Migration_6to7_ProductBaseSchema)

        db.apply {
            val cursor = query("SELECT input_method, package_price, package_quantity, package_unit FROM products WHERE productId = 1")
            cursor.moveToFirst()

            // Verify new field defaults
            assert(cursor.getString(0) == "UNIT") { "input_method should default to 'UNIT'" }
            assert(cursor.isNull(1)) { "package_price should be null" }
            assert(cursor.isNull(2)) { "package_quantity should be null" }
            assert(cursor.isNull(3)) { "package_unit should be null" }

            cursor.close()
            close()
        }
    }

    companion object {
        private const val TEST_DB = "migration-test"
    }
}
