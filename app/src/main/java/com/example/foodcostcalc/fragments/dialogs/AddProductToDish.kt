package com.example.foodcostcalc.fragments.dialogs

import android.app.Application
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.foodcostcalc.R
import com.example.foodcostcalc.model.Dish
import com.example.foodcostcalc.model.ProductIncluded
import com.example.foodcostcalc.viewmodel.AddViewModel

class AddProductToDish: DialogFragment(), AdapterView.OnItemSelectedListener {
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
        val view: View = inflater.inflate(R.layout.add_products_to_dish,container,false)
        /** initialize ui with viewmodel*/
        val viewModel = ViewModelProvider(this).get(AddViewModel::class.java)

        /** binders*/
        val weightOfAddedProduct = view.findViewById<EditText>(R.id.product_weight)
        val addProductToDishBtn  = view.findViewById<ImageButton>(R.id.add_product_to_dish)
        val productSpinner = view.findViewById<Spinner>(R.id.mySpinner)
        val dishSpinner = view.findViewById<Spinner>(R.id.dishSpinner)

        /** ADAPTERs FOR SPINNERs */
        val productList = mutableListOf<String>()
        viewModel.readAllProductData.observe(viewLifecycleOwner, Observer { it.forEach{product -> productList.add(product.name) } })
        val productAdapter = ArrayAdapter(requireActivity(),R.layout.spinner_layout,productList)
        productAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
       with(productSpinner)
        {
            adapter = productAdapter
            setSelection(0, false)
            onItemSelectedListener = this@AddProductToDish
            prompt = "Select product"
            gravity = Gravity.CENTER
        }
        productSpinner.id = NEW_SPINNER_ID
        productAdapter.notifyDataSetChanged()

        val dishList = mutableListOf<String>()
        viewModel.readAllDishData.observe(viewLifecycleOwner, Observer { it.forEach{dish -> dishList.add(dish.name)} })
        val dishesAdapter = ArrayAdapter(requireActivity(),R.layout.spinner_layout,dishList)
        dishesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        with(dishSpinner){
            adapter = dishesAdapter
            setSelection(0,false)
            onItemSelectedListener = this@AddProductToDish
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


        /** BUTTON LOGIC*/
        addProductToDishBtn.setOnClickListener {
            if (weightOfAddedProduct.text.isNullOrEmpty()) { showToast(message = "You can't add product without weight.")
            } else {
                val chosenDish      = viewModel.readAllDishData.value?.get(dishPosition!!)
                val chosenProduct   = viewModel.readAllProductData.value?.get(productPosition!!)
                val weight          = weightOfAddedProduct.text.toString().toDouble() / 1000
                viewModel.addProductToDish(ProductIncluded(0, chosenProduct!!, chosenDish!!.dishId, chosenProduct.productId, weight))
            }
            weightOfAddedProduct.text.clear()
            Toast.makeText(requireContext(), "${viewModel.readAllProductData.value?.get(productPosition!!)?.name} added.", Toast.LENGTH_SHORT).show()
        }


        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        return view



    }
    companion object {
        fun newInstance(): AddProductToDish =
            AddProductToDish()
        const val TAG = "AddProductToDish"
    }



}


