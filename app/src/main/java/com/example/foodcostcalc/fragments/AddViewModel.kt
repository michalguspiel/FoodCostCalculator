package com.example.foodcostcalc.fragments

import androidx.lifecycle.ViewModel
import com.example.foodcostcalc.model.Dish
import com.example.foodcostcalc.model.Product
import com.example.foodcostcalc.data.Repository
import kotlin.properties.Delegates

class AddViewModel(private val repository: Repository)
    : ViewModel() {

    fun getProducts()                 = repository.getProduct()

    fun addProducts(product: Product) = repository.addProduct(product)

    fun getDishes()                   = repository.getDishes()

    fun addDishes(dish: Dish)         = repository.addDish(dish)

    fun addProductToDish(dish: Dish,product: Product, weight: Double) = repository.addProductToDish(dish,product,weight)

    fun editProduct(newProduct: Product, oldProduct: Product) = repository.editProduct(newProduct,oldProduct)

    fun deleteProduct(product: Product) = repository.deleteProduct(product)

    fun setPosition(pos: Int) = repository.setPosition(pos)

    fun getPosition() = repository.getPosition()

    fun setSecondPosition(pos: Int) = repository.setSecondPosition(pos)

    fun getSecondPosition() = repository.getSecondPosition()

    fun setFlag(boolean: Boolean) = repository.setFlag(boolean)

    fun getFlag() = repository.getFlag()

    fun editDish(dish:Dish ,listOfProducts: MutableList<Pair<Product, Double>>) = repository.editDish(dish,listOfProducts)

    fun deleteDish(dish: Dish) = repository.deleteDish(dish)

    fun deleteProductFromDish(dish:Dish ,product: Product) = repository.deleteProductFromDish(dish, product)
}