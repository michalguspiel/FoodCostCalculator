# Repository Unit Tests

This directory contains comprehensive unit tests for all repository classes in the Food Cost Calculator Android application.

## Test Files

### ProductRepositoryTest.kt
Tests all public methods of `ProductRepository`:
- `products` - Flow property returning all products
- `getProduct(id: Long)` - Returns specific product by ID
- `addProduct(product: ProductBase)` - Adds new product and returns ID
- `addProductDish(productDish: ProductDish)` - Associates product with dish
- `editProduct(newProduct: ProductBase)` - Updates existing product
- `deleteProduct(id: Long)` - Deletes product by ID

**Test Scenarios:**
- Unit and package pricing input methods
- Different measurement units (GRAM, KILOGRAM, MILLILITER, etc.)
- Edge cases like empty product lists
- Non-existent product handling

### DishRepositoryTest.kt
Tests all public methods of `DishRepository`:
- `dishes` - Flow property returning all complete dishes
- `getDish(id: Long)` - Returns specific complete dish by ID
- `getDishCount()` - Returns total number of dishes
- `addDish(dish: DishBase)` - Adds new dish and returns ID
- `deleteDish(dishId: Long)` - Deletes dish by ID
- `updateDish(dish: DishBase)` - Updates existing dish
- `updateDishRecipe(recipeId: Long, dishId: Long)` - Links recipe to dish
- `deleteProductDish(productDish: ProductDish)` - Removes product from dish
- `deleteHalfProductDish(halfProductDish: HalfProductDish)` - Removes half product from dish
- `updateProductDish(productDish: ProductDish)` - Updates product-dish association
- `updateHalfProductDish(halfProductDish: HalfProductDish)` - Updates half product-dish association

**Test Scenarios:**
- Dishes with and without recipes
- Different margin percentages and tax rates
- Empty dish lists
- Zero dish counts

### HalfProductRepositoryTest.kt
Tests all public methods of `HalfProductRepository`:
- `completeHalfProducts` - Flow property returning all complete half products
- `halfProducts` - Flow property returning all half product bases
- `getCompleteHalfProduct(id: Long)` - Returns specific complete half product by ID
- `addHalfProduct(halfProductBase: HalfProductBase)` - Adds new half product
- `addHalfProductDish(halfProductDish: HalfProductDish)` - Associates half product with dish
- `addProductHalfProduct(productHalfProduct: ProductHalfProduct)` - Associates product with half product
- `updateHalfProduct(halfProductBase: HalfProductBase)` - Updates existing half product
- `deleteHalfProduct(id: Long)` - Deletes half product by ID
- `deleteProductHalfProduct(productHalfProduct: ProductHalfProduct)` - Removes product from half product
- `updateProductHalfProduct(productHalfProduct: ProductHalfProduct)` - Updates product-half product association

**Test Scenarios:**
- Different measurement units (weight, volume, count)
- Product associations with and without weight pieces
- Empty half product lists

### RecipeRepositoryTest.kt
Tests all public methods of `RecipeRepository`:
- `upsertRecipe(recipe: Recipe)` - Inserts or updates recipe and returns ID
- `upsertRecipeSteps(recipeStep: List<RecipeStep>)` - Inserts or updates recipe steps
- `getRecipeWithSteps(recipeId: Long)` - Returns recipe with all its steps
- `deleteRecipeStepsByIds(ids: List<Long>)` - Deletes specific recipe steps by IDs

**Test Scenarios:**
- Recipes with all optional fields populated
- Recipes with minimal fields (nulls)
- Empty step lists
- Single step recipes
- Large number of steps
- Recipes without steps

## Testing Framework

### Libraries Used
- **MockK** (v1.13.12) - For mocking DAO dependencies
- **KoTest** (v5.9.0) - For idiomatic assertions
- **Coroutines Test** (v1.9.0) - For suspend function testing
- **JUnit 4** (v4.13.2) - Base testing framework

### Testing Patterns
- `runTest { }` blocks for all suspend function tests
- `mockk<DaoType>(relaxed = true)` for DAO mocking
- `coEvery { ... } returns ...` for suspend function stubbing
- `coVerify { ... }` for suspend function call verification
- `every { ... } returns flowOf(...)` for Flow stubbing
- `result shouldBe expected` for KoTest assertions

### Test Structure
Each test follows the Given-When-Then pattern:
```kotlin
@Test
fun `methodName should expectedBehavior when condition`() = runTest {
    // Given
    val input = createTestData()
    coEvery { dao.method(input) } returns expectedResult
    
    // When
    val result = repository.method(input)
    
    // Then
    result shouldBe expectedResult
    coVerify { dao.method(input) }
}
```

### Coverage
These tests provide comprehensive coverage of:
- All public repository methods
- Flow properties
- Suspend function calls
- DAO method verification
- Parameter passing
- Return value handling
- Edge cases and error scenarios

The tests ensure that the repositories correctly delegate to their respective DAOs and handle all expected data types and scenarios within the Food Cost Calculator domain.