package com.erdees.foodcostcalc.data.db.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class Migration_1to2_RefactorDatabase : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // PRODUCT_DISH
        // Create the new table
        db.execSQL(
            "CREATE TABLE Product_Dish (" +
                    "productDishId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "productId INTEGER NOT NULL, " +
                    "dishId INTEGER NOT NULL, " +
                    "quantity REAL NOT NULL, " +
                    "quantityUnit TEXT NOT NULL," +
                    "FOREIGN KEY(productId) REFERENCES products(productId) ON DELETE CASCADE, " +
                    "FOREIGN KEY(dishId) REFERENCES dishes(dishId) ON DELETE CASCADE)"
        )

        // Create indices
        db.execSQL("CREATE INDEX index_Product_Dish_productId ON Product_Dish(productId)")
        db.execSQL("CREATE INDEX index_Product_Dish_dishId ON Product_Dish(dishId)")

        // Copy the data
        db.execSQL(
            "INSERT INTO Product_Dish (productId, dishId, quantity, quantityUnit) " +
                    "SELECT productOwnerId, dishOwnerId, weight, weightUnit " +
                    "FROM ProductIncluded"
        )

        // Remove the old table
        db.execSQL("DROP TABLE ProductIncluded")

        // PRODUCT_HALFPRODUCT
        // Create the new table
        db.execSQL(
            "CREATE TABLE Product_HalfProduct (" +
                    "productHalfProductId INTEGER PRIMARY KEY NOT NULL, " +
                    "productId INTEGER NOT NULL, " +
                    "halfProductId INTEGER NOT NULL, " +
                    "quantity REAL NOT NULL, " +
                    "quantityUnit TEXT NOT NULL, " +
                    "weightPiece REAL, " +
                    "FOREIGN KEY(productId) REFERENCES products(productId) ON DELETE CASCADE, " +
                    "FOREIGN KEY(halfProductId) REFERENCES HalfProduct(halfProductId) ON DELETE CASCADE)"
        )

        // Create indices
        db.execSQL("CREATE INDEX index_Product_HalfProduct_productId ON Product_HalfProduct(productId)")
        db.execSQL("CREATE INDEX index_Product_HalfProduct_halfProductId ON Product_HalfProduct(halfProductId)")

        // Copy the data
        db.execSQL(
            "INSERT INTO Product_HalfProduct (productId, halfProductId, quantity, quantityUnit, weightPiece) " +
                    "SELECT  productId, halfProductId, weight, weightUnit, weightOfPiece " +
                    "FROM ProductIncludedInHalfProduct"
        )

        // Remove the old table
        db.execSQL("DROP TABLE ProductIncludedInHalfProduct")

        // HALFPRODUCT_DISH
        // Create the new table
        db.execSQL(
            "CREATE TABLE HalfProduct_Dish (" +
                    "halfProductDishId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "halfProductId INTEGER NOT NULL, " +
                    "dishId INTEGER NOT NULL, " +
                    "quantity REAL NOT NULL, " +
                    "quantityUnit TEXT NOT NULL, " +
                    "FOREIGN KEY(halfProductId) REFERENCES  HalfProduct(halfProductId) ON DELETE CASCADE, " +
                    "FOREIGN KEY(dishId) REFERENCES dishes(dishId) ON DELETE CASCADE)"
        )

        // Create indices
        db.execSQL("CREATE INDEX index_HalfProduct_Dish_halfProductId ON HalfProduct_Dish(halfProductId)")
        db.execSQL("CREATE INDEX index_HalfProduct_Dish_dishId ON HalfProduct_Dish(dishId)")

        // Copy the data
        db.execSQL(
            "INSERT INTO HalfProduct_Dish (halfProductId, dishId, quantity, quantityUnit) " +
                    "SELECT halfProductOwnerId, dishOwnerId, weight, unit " +
                    "FROM HalfProductIncludedInDish"
        )

        // Remove the old table
        db.execSQL("DROP TABLE HalfProductIncludedInDish")
    }
}
