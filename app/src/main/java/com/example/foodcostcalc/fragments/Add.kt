package com.example.foodcostcalc.fragments

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.foodcostcalc.R
import com.example.foodcostcalc.model.ProductIncluded
import com.example.foodcostcalc.fragments.dialogs.CreateDish
import com.example.foodcostcalc.model.Product
import com.example.foodcostcalc.viewmodel.AddViewModel


class Add : Fragment(), AdapterView.OnItemSelectedListener {
    private val NEW_SPINNER_ID = 1
    private val ANOTHER_SPINNER_ID = 2
    private var productPosition: Int? = null
    private var dishPosition: Int? = null

    private fun showToast(context: FragmentActivity? = activity, message: String, duration: Int = Toast.LENGTH_LONG) {
        Toast.makeText(context, message, duration) .show()
    }
    /**Spinner implementation TODO */

    override fun onNothingSelected(parent: AdapterView<*>?) {
        showToast(activity,"nothing selected ",3)
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (parent?.id) {
            1 -> {productPosition = position
            }
            else -> {dishPosition = position
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_add, container, false)
        val viewModel = ViewModelProvider(this).get(AddViewModel::class.java)


            /** BINDERS FOR BUTTONS AND FIELDS */
            val name                 = view.findViewById<EditText>(R.id.product_name)
            val price                = view.findViewById<EditText>(R.id.product_price)
            val tax                  = view.findViewById<EditText>(R.id.product_tax)
            val waste                = view.findViewById<EditText>(R.id.product_waste)
            val weightOfAddedProduct = view.findViewById<EditText>(R.id.product_weight)
            val addButton            = view.findViewById<Button>(R.id.addProduct)
            val createDishButton     = view.findViewById<Button>(R.id.new_dish_button)
            val addProductToDishBtn  = view.findViewById<ImageButton>(R.id.add_product_to_dish)



            /** ADAPTERs FOR SPINNERs */
            val productList = mutableListOf<String>()
            viewModel.readAllProductData.observe(viewLifecycleOwner, Observer { it.forEach{product -> productList.add(product.name) } })
            val productAdapter = ArrayAdapter(requireActivity(),R.layout.spinner_layout,productList)
        productAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
             val mySpinner = view.findViewById<Spinner>(R.id.mySpinner)
            with(mySpinner)
            {
                adapter = productAdapter
                setSelection(0, false)
                onItemSelectedListener = this@Add
                prompt = "Select product"
                gravity = Gravity.CENTER
            }
            mySpinner.id = NEW_SPINNER_ID
            productAdapter.notifyDataSetChanged()

            val dishList = mutableListOf<String>()
            viewModel.readAllDishData.observe(viewLifecycleOwner, Observer { it.forEach{dish -> dishList.add(dish.name)} })
            val dishesAdapter = ArrayAdapter(requireActivity(),R.layout.spinner_layout,dishList)
        dishesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            val dishSpinner = view.findViewById<Spinner>(R.id.dishSpinner)
            with(dishSpinner){
                adapter = dishesAdapter
                setSelection(0,false)
                onItemSelectedListener = this@Add
                prompt = "Select dish"
                gravity = Gravity.CENTER
            }
            dishSpinner.id =  ANOTHER_SPINNER_ID
        dishesAdapter.notifyDataSetChanged()


            /**OBSERVING LIVEDATA FROM ADDVIEWMODEL
             *  WHICH OBSERVES LIVEDATA IN REPOSITORY
             *  WHICH OBSERVES LIVEDATA FROM DAO*/

            viewModel.readAllProductData.observe(viewLifecycleOwner, Observer { products ->
                productAdapter.clear()
                products.forEach { product ->
                    productAdapter.add(product.name)
                    productAdapter.notifyDataSetChanged()
                }
            })
            viewModel.readAllDishData.observe(viewLifecycleOwner, Observer { dishes ->
                dishesAdapter.clear()
                dishes.forEach{dish ->
                    dishesAdapter.add(dish.name)
                    dishesAdapter.notifyDataSetChanged()
                }

            })

            /** BUTTONS FUNCTIONALITY */

            createDishButton.setOnClickListener{
                CreateDish().show(childFragmentManager,
                    CreateDish.TAG)
            }

            addButton.setOnClickListener{
                if(name.text.isNullOrEmpty()||
                    price.text.isNullOrEmpty()||
                    tax.text.isNullOrEmpty()||
                    waste.text.isNullOrEmpty()){showToast(message = "Fill all data!")}
                else {
                    val product = Product(0,
                        name.text.toString(),
                        price.text.toString().toDouble(),
                        tax.text.toString().toDouble(),
                        waste.text.toString().toDouble()
                    )
                    viewModel.addProducts(product)

                    name.text.clear()
                    price.text.clear()
                    tax.text.clear()
                    waste.text.clear()
                }
            }
            addProductToDishBtn.setOnClickListener {
                if (weightOfAddedProduct.text.isNullOrEmpty()) { showToast(message = "You can't add product without weight.")
                } else {
                    val chosenDish      = viewModel.readAllDishData.value?.get(dishPosition!!)
                    val chosenProduct   = viewModel.readAllProductData.value?.get(productPosition!!)
                    val weight          = weightOfAddedProduct.text.toString().toDouble() / 1000
                    viewModel.addProductToDish(ProductIncluded(0, chosenProduct!!, chosenDish!!.dishId, chosenProduct.productId, weight))
                    }
                    weightOfAddedProduct.text.clear()
                }





        return view
    }
    companion object {
        fun newInstance():Add = Add()
        const val TAG = "Add"
    }

}

