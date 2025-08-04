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
            // Insert real product data from the app
            execSQL("""
                INSERT INTO products (productId, product_name, pricePerUnit, tax, waste, unit) 
                VALUES 
                (1, 'Minced Beef', 19.2, 0, 0, 'per kilogram'),
                (2, 'Burger Bun', 0.7, 0, 0, 'per piece'),
                (3, 'Cheese Slice', 0.5, 0, 0, 'per piece'),
                (4, 'Lettuce', 3.99, 0, 15, 'per kilogram'),
                (5, 'foo', 15, 0, 5, 'per kilogram'),
                (6, 'Minced Beef', 19.2, 0, 0, 'per kilogram'),
                (7, 'Burger Bun', 0.7, 0, 0, 'per piece'),
                (8, 'Cheese Slice', 0.5, 0, 0, 'per piece'),
                (9, 'Lettuce', 3.99, 0, 15, 'per kilogram'),
                (10, 'Minced Beef', 19.2, 0, 0, 'per kilogram'),
                (11, 'Burger Bun', 0.7, 0, 0, 'per piece'),
                (12, 'Cheese Slice', 0.5, 0, 0, 'per piece'),
                (13, 'Lettuce', 3.99, 0, 15, 'per kilogram'),
                (14, 'Minced Beef', 19.2, 0, 0, 'per kilogram'),
                (15, 'Burger Bun', 0.7, 0, 0, 'per piece'),
                (16, 'Cheese Slice', 0.5, 0, 0, 'per piece'),
                (17, 'Lettuce', 3.99, 0, 15, 'per kilogram'),
                (18, 'Minced Beef', 19.2, 0, 0, 'per kilogram'),
                (19, 'Burger Bun', 0.7, 0, 0, 'per piece'),
                (20, 'Cheese Slice', 0.5, 0, 0, 'per piece'),
                (21, 'Lettuce', 3.99, 0, 15, 'per kilogram')
            """.trimIndent())

            // Insert dishes for the foreign key relationships
            execSQL("""
                INSERT INTO dishes (dishId, dish_name, margin_percent, dish_tax) 
                VALUES 
                (1, 'Burger Dish 1', 100.0, 0.0),
                (2, 'Fish Special', 120.0, 5.0),
                (3, 'Burger Dish 3', 110.0, 2.0),
                (4, 'Burger Dish 4', 105.0, 1.0),
                (5, 'Burger Dish 5', 115.0, 3.0)
            """.trimIndent())

            // Insert real HalfProduct data
            execSQL("""
                INSERT INTO HalfProduct (halfProductId, name, halfProductUnit) 
                VALUES 
                (1, 'Kebab Sauce', 'per liter'),
                (2, 'Fish 🐟 ', 'per piece'),
                (3, 'Nut Mix', 'per kilogram'),
                (4, 'Pound', 'per pound'),
                (5, 'Gallon', 'per gallon')
            """.trimIndent())

            // Insert real Product_Dish data
            execSQL("""
                INSERT INTO Product_Dish (productDishId, productId, dishId, quantity, quantityUnit) 
                VALUES 
                (1, 1, 1, 150, 'gram'),
                (2, 2, 1, 1, 'piece'),
                (3, 3, 1, 1, 'piece'),
                (4, 4, 1, 20, 'gram'),
                (5, 5, 2, 5, 'kilogram'),
                (6, 6, 3, 150, 'gram'),
                (7, 7, 3, 1, 'piece'),
                (8, 8, 3, 1, 'piece'),
                (9, 9, 3, 20, 'gram'),
                (10, 10, 4, 150, 'gram'),
                (11, 11, 4, 1, 'piece'),
                (12, 12, 4, 1, 'piece'),
                (13, 13, 4, 20, 'gram'),
                (14, 14, 5, 150, 'gram'),
                (15, 15, 5, 1, 'piece'),
                (16, 16, 5, 1, 'piece'),
                (17, 17, 5, 20, 'gram')
            """.trimIndent())

            // Insert real Product_HalfProduct data
            execSQL("""
                INSERT INTO Product_HalfProduct (productHalfProductId, productId, halfProductId, quantity, quantityUnit, weightPiece) 
                VALUES 
                (1, 4, 2, 1, 'kilogram', 1),
                (2, 3, 1, 1, 'piece', 0.09),
                (3, 2, 3, 1, 'piece', 0.1)
            """.trimIndent())

            // Insert real HalfProduct_Dish data
            execSQL("""
                INSERT INTO HalfProduct_Dish (halfProductDishId, halfProductId, dishId, quantity, quantityUnit) 
                VALUES 
                (1, 2, 2, 1, 'piece'),
                (2, 1, 2, 500, 'milliliter')
            """.trimIndent())

            close()
        }

        // Migrate the database to version 6
        val db = helper.runMigrationsAndValidate(TEST_DB, 6, true, Migration_5to6_UnitEnumMigration)

        // Verify the migration results
        db.apply {
            // Test products table unit conversion - all should convert to proper enum values
            val productsCursor = query("SELECT productId, unit FROM products ORDER BY productId")

            while (productsCursor.moveToNext()) {
                val productId = productsCursor.getInt(0)
                val unit = productsCursor.getString(1)

                // All "per kilogram" should become KILOGRAM, all "per piece" should become PIECE
                when (productId) {
                    in listOf(1, 4, 5, 6, 9, 10, 13, 14, 17, 18, 21) -> {
                        assert(unit == MeasurementUnit.KILOGRAM.name) {
                            "Product $productId: expected KILOGRAM but got $unit"
                        }
                    }
                    in listOf(2, 3, 7, 8, 11, 12, 15, 16, 19, 20) -> {
                        assert(unit == MeasurementUnit.PIECE.name) {
                            "Product $productId: expected PIECE but got $unit"
                        }
                    }
                }
            }
            productsCursor.close()

            // Test HalfProduct table unit conversion
            val halfProductsCursor = query("SELECT halfProductId, halfProductUnit FROM HalfProduct ORDER BY halfProductId")
            val expectedHalfProductUnits = mapOf(
                1 to MeasurementUnit.LITER.name,     // per liter
                2 to MeasurementUnit.PIECE.name,     // per piece
                3 to MeasurementUnit.KILOGRAM.name,  // per kilogram
                4 to MeasurementUnit.POUND.name,     // per pound
                5 to MeasurementUnit.GALLON.name     // per gallon
            )

            while (halfProductsCursor.moveToNext()) {
                val halfProductId = halfProductsCursor.getInt(0)
                val unit = halfProductsCursor.getString(1)
                val expectedUnit = expectedHalfProductUnits[halfProductId]
                assert(unit == expectedUnit) {
                    "HalfProduct $halfProductId: expected unit $expectedUnit but got $unit"
                }
            }
            halfProductsCursor.close()

            // Test Product_Dish table unit conversion
            val productDishCursor = query("SELECT productDishId, quantityUnit FROM Product_Dish ORDER BY productDishId")
            val expectedProductDishUnits = mapOf(
                1 to MeasurementUnit.GRAM.name,      // gram
                2 to MeasurementUnit.PIECE.name,     // piece
                3 to MeasurementUnit.PIECE.name,     // piece
                4 to MeasurementUnit.GRAM.name,      // gram
                5 to MeasurementUnit.KILOGRAM.name,  // kilogram
                6 to MeasurementUnit.GRAM.name,      // gram
                7 to MeasurementUnit.PIECE.name,     // piece
                8 to MeasurementUnit.PIECE.name,     // piece
                9 to MeasurementUnit.GRAM.name,      // gram
                10 to MeasurementUnit.GRAM.name,     // gram
                11 to MeasurementUnit.PIECE.name,    // piece
                12 to MeasurementUnit.PIECE.name,    // piece
                13 to MeasurementUnit.GRAM.name,     // gram
                14 to MeasurementUnit.GRAM.name,     // gram
                15 to MeasurementUnit.PIECE.name,    // piece
                16 to MeasurementUnit.PIECE.name,    // piece
                17 to MeasurementUnit.GRAM.name      // gram
            )

            while (productDishCursor.moveToNext()) {
                val productDishId = productDishCursor.getInt(0)
                val unit = productDishCursor.getString(1)
                val expectedUnit = expectedProductDishUnits[productDishId]
                assert(unit == expectedUnit) {
                    "Product_Dish $productDishId: expected unit $expectedUnit but got $unit"
                }
            }
            productDishCursor.close()

            // Test Product_HalfProduct table unit conversion
            val productHalfProductCursor = query("SELECT productHalfProductId, quantityUnit FROM Product_HalfProduct ORDER BY productHalfProductId")
            val expectedProductHalfProductUnits = mapOf(
                1 to MeasurementUnit.KILOGRAM.name,  // kilogram
                2 to MeasurementUnit.PIECE.name,     // piece
                3 to MeasurementUnit.PIECE.name      // piece
            )

            while (productHalfProductCursor.moveToNext()) {
                val productHalfProductId = productHalfProductCursor.getInt(0)
                val unit = productHalfProductCursor.getString(1)
                val expectedUnit = expectedProductHalfProductUnits[productHalfProductId]
                assert(unit == expectedUnit) {
                    "Product_HalfProduct $productHalfProductId: expected unit $expectedUnit but got $unit"
                }
            }
            productHalfProductCursor.close()

            // Test HalfProduct_Dish table unit conversion
            val halfProductDishCursor = query("SELECT halfProductDishId, quantityUnit FROM HalfProduct_Dish ORDER BY halfProductDishId")
            val expectedHalfProductDishUnits = mapOf(
                1 to MeasurementUnit.PIECE.name,       // piece
                2 to MeasurementUnit.MILLILITER.name   // milliliter
            )

            while (halfProductDishCursor.moveToNext()) {
                val halfProductDishId = halfProductDishCursor.getInt(0)
                val unit = halfProductDishCursor.getString(1)
                val expectedUnit = expectedHalfProductDishUnits[halfProductDishId]
                assert(unit == expectedUnit) {
                    "HalfProduct_Dish $halfProductDishId: expected unit $expectedUnit but got $unit"
                }
            }
            halfProductDishCursor.close()

            // Verify that all data counts are preserved
            val productsDataCursor = query("SELECT COUNT(*) FROM products")
            productsDataCursor.moveToFirst()
            assert(productsDataCursor.getInt(0) == 21) { "Products count mismatch - expected 21" }
            productsDataCursor.close()

            val halfProductsDataCursor = query("SELECT COUNT(*) FROM HalfProduct")
            halfProductsDataCursor.moveToFirst()
            assert(halfProductsDataCursor.getInt(0) == 5) { "HalfProducts count mismatch - expected 5" }
            halfProductsDataCursor.close()

            val productDishDataCursor = query("SELECT COUNT(*) FROM Product_Dish")
            productDishDataCursor.moveToFirst()
            assert(productDishDataCursor.getInt(0) == 17) { "Product_Dish count mismatch - expected 17" }
            productDishDataCursor.close()

            val productHalfProductDataCursor = query("SELECT COUNT(*) FROM Product_HalfProduct")
            productHalfProductDataCursor.moveToFirst()
            assert(productHalfProductDataCursor.getInt(0) == 3) { "Product_HalfProduct count mismatch - expected 3" }
            productHalfProductDataCursor.close()

            val halfProductDishDataCursor = query("SELECT COUNT(*) FROM HalfProduct_Dish")
            halfProductDishDataCursor.moveToFirst()
            assert(halfProductDishDataCursor.getInt(0) == 2) { "HalfProduct_Dish count mismatch - expected 2" }
            halfProductDishDataCursor.close()

            // Verify that product names and other data are preserved correctly
            val sampleProductCursor = query("SELECT product_name, pricePerUnit FROM products WHERE productId = 1")
            sampleProductCursor.moveToFirst()
            assert(sampleProductCursor.getString(0) == "Minced Beef") { "Product name not preserved" }
            assert(sampleProductCursor.getDouble(1) == 19.2) { "Product price not preserved" }
            sampleProductCursor.close()

            // Verify that HalfProduct names are preserved (including emoji)
            val halfProductNameCursor = query("SELECT name FROM HalfProduct WHERE halfProductId = 2")
            halfProductNameCursor.moveToFirst()
            assert(halfProductNameCursor.getString(0) == "Fish 🐟 ") { "HalfProduct name with emoji not preserved" }
            halfProductNameCursor.close()

            // Verify schema integrity - check that all tables still exist and have correct structure
            val tablesCursor = query("SELECT name FROM sqlite_master WHERE type='table' ORDER BY name")
            val expectedTables = setOf("products", "dishes", "HalfProduct", "Product_Dish", "Product_HalfProduct", "HalfProduct_Dish", "Recipe", "Recipe_Step", "feature_requests", "upvoted_feature_requests")
            val actualTables = mutableSetOf<String>()

            while (tablesCursor.moveToNext()) {
                val tableName = tablesCursor.getString(0)
                if (!tableName.startsWith("android_") && !tableName.startsWith("sqlite_")) {
                    actualTables.add(tableName)
                }
            }
            tablesCursor.close()

            assert(actualTables.containsAll(expectedTables)) {
                "Missing tables. Expected: $expectedTables, Actual: $actualTables"
            }

            close()
        }
    }

    @Test
    fun testMigration_6_to_7() {
        // Create the database with version 6 schema and insert test data
        helper.createDatabase(TEST_DB, 6).apply {
            // Insert test products with the old schema (pricePerUnit)
            execSQL("""
                INSERT INTO products (productId, product_name, pricePerUnit, tax, waste, unit) 
                VALUES 
                (1, 'Minced Beef', 19.2, 0.0, 0.0, 'KILOGRAM'),
                (2, 'Burger Bun', 0.7, 5.0, 2.5, 'PIECE'),
                (3, 'Cheese Slice', 0.5, 10.0, 1.0, 'PIECE'),
                (4, 'Lettuce', 3.99, 0.0, 15.0, 'KILOGRAM'),
                (5, 'Olive Oil', 12.5, 8.0, 0.0, 'LITER')
            """.trimIndent())

            close()
        }

        // Migrate the database to version 7
        val db = helper.runMigrationsAndValidate(TEST_DB, 7, true, Migration_6to7_ProductBaseSchema)

        // Verify the migration results
        db.apply {
            // Test that all data is preserved and new fields are correctly set
            val productsCursor = query("SELECT productId, product_name, input_method, package_price, package_quantity, package_unit, price_per_unit, unit, tax, waste FROM products ORDER BY productId")

            val expectedProducts = listOf(
                Triple(1L, "Minced Beef", 19.2),
                Triple(2L, "Burger Bun", 0.7),
                Triple(3L, "Cheese Slice", 0.5),
                Triple(4L, "Lettuce", 3.99),
                Triple(5L, "Olive Oil", 12.5)
            )

            var index = 0
            while (productsCursor.moveToNext()) {
                val productId = productsCursor.getLong(0)
                val productName = productsCursor.getString(1)
                val inputMethod = productsCursor.getString(2)
                val packagePrice = if (productsCursor.isNull(3)) null else productsCursor.getDouble(3)
                val packageQuantity = if (productsCursor.isNull(4)) null else productsCursor.getDouble(4)
                val packageUnit = if (productsCursor.isNull(5)) null else productsCursor.getString(5)
                val pricePerUnit = productsCursor.getDouble(6)
                val unit = productsCursor.getString(7)
                val tax = productsCursor.getDouble(8)
                val waste = productsCursor.getDouble(9)

                val expected = expectedProducts[index]

                // Verify data integrity
                assert(productId == expected.first) { "Product ID mismatch: expected ${expected.first}, got $productId" }
                assert(productName == expected.second) { "Product name mismatch: expected ${expected.second}, got $productName" }
                assert(pricePerUnit == expected.third) { "Price per unit mismatch: expected ${expected.third}, got $pricePerUnit" }

                // Verify new fields
                assert(inputMethod == "UNIT") { "Input method should default to UNIT, got $inputMethod" }
                assert(packagePrice == null) { "Package price should be null for migrated data, got $packagePrice" }
                assert(packageQuantity == null) { "Package quantity should be null for migrated data, got $packageQuantity" }
                assert(packageUnit == null) { "Package unit should be null for migrated data, got $packageUnit" }

                // Verify existing fields are preserved
                when (productId) {
                    1L -> {
                        assert(tax == 0.0) { "Tax mismatch for product 1" }
                        assert(waste == 0.0) { "Waste mismatch for product 1" }
                        assert(unit == "KILOGRAM") { "Unit mismatch for product 1" }
                    }
                    2L -> {
                        assert(tax == 5.0) { "Tax mismatch for product 2" }
                        assert(waste == 2.5) { "Waste mismatch for product 2" }
                        assert(unit == "PIECE") { "Unit mismatch for product 2" }
                    }
                    3L -> {
                        assert(tax == 10.0) { "Tax mismatch for product 3" }
                        assert(waste == 1.0) { "Waste mismatch for product 3" }
                        assert(unit == "PIECE") { "Unit mismatch for product 3" }
                    }
                    4L -> {
                        assert(tax == 0.0) { "Tax mismatch for product 4" }
                        assert(waste == 15.0) { "Waste mismatch for product 4" }
                        assert(unit == "KILOGRAM") { "Unit mismatch for product 4" }
                    }
                    5L -> {
                        assert(tax == 8.0) { "Tax mismatch for product 5" }
                        assert(waste == 0.0) { "Waste mismatch for product 5" }
                        assert(unit == "LITER") { "Unit mismatch for product 5" }
                    }
                }

                index++
            }
            productsCursor.close()

            // Verify that all products were migrated
            assert(index == 5) { "Expected 5 products, but got $index" }

            // Verify data count is preserved
            val countCursor = query("SELECT COUNT(*) FROM products")
            countCursor.moveToFirst()
            assert(countCursor.getInt(0) == 5) { "Products count mismatch - expected 5" }
            countCursor.close()

            // Verify table structure - check that the old column doesn't exist and new columns do exist
            val pragmaCursor = query("PRAGMA table_info(products)")
            val columns = mutableSetOf<String>()
            while (pragmaCursor.moveToNext()) {
                columns.add(pragmaCursor.getString(1)) // Column name is at index 1
            }
            pragmaCursor.close()

            // Verify new schema
            val expectedColumns = setOf(
                "productId", "product_name", "input_method", "package_price",
                "package_quantity", "package_unit", "price_per_unit", "unit",
                "tax", "waste"
            )

            assert(columns.containsAll(expectedColumns)) {
                "Missing expected columns. Expected: $expectedColumns, Found: $columns"
            }

            // Verify old column is gone
            assert(!columns.contains("pricePerUnit")) {
                "Old column 'pricePerUnit' should not exist after migration"
            }

            close()
        }
    }

    companion object {
        private const val TEST_DB = "migration-test"
    }
}
