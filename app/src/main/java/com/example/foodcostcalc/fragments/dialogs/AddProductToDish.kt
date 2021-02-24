@file:Suppress("PrivatePropertyName")

package com.example.foodcostcalc.fragments.dialogs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.foodcostcalc.R
import com.example.foodcostcalc.SharedPreferences
import com.example.foodcostcalc.changeUnitList
import com.example.foodcostcalc.model.ProductIncluded
import com.example.foodcostcalc.setAdapterList
import com.example.foodcostcalc.viewmodel.AddViewModel

class AddProductToDish : DialogFragment(), AdapterView.OnItemSelectedListener {
    private val PRODUCT_SPINNER_ID = 1
    private val DISH_SPINNER_ID = 2
    private val UNIT_SPINNER_ID = 3
    private var productPosition: Int? = null
    private var dishPosition: Int? = null
    private val unitList = arrayListOf<String>() // list for units, to populate spinner
    private var chosenUnit: String = ""

    /** Initialized here so it can be called outside of 'onCreateView' */
    lateinit var viewModel: ViewModel
    private lateinit var unitAdapter: ArrayAdapter<*>
    private lateinit var unitSpinner: Spinner

    /**Holder for booleans*/
    private var metricAsBoolean = true
    private var usaAsBoolean = true

    /**Holder for type of units*/
    private var unitType = ""


    private fun showToast(
        context: FragmentActivity? = activity,
        message: String,
        duration: Int = Toast.LENGTH_LONG
    ) {
        Toast.makeText(context, message, duration).show()
    }


    /**Spinner implementation */

    override fun onNothingSelected(parent: AdapterView<*>?) {
        this.dismiss()
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (parent?.id) {
            1 -> {
                productPosition = position
                unitType = setAdapterList(viewModel as AddViewModel,position)
                unitList.changeUnitList(unitType,metricAsBoolean,usaAsBoolean)
                unitAdapter.notifyDataSetChanged()
                unitSpinner.setSelection(0, false)
                chosenUnit = unitList.first()
                unitSpinner.setSelection(0) // when the product is chosen first units got chosen immediately
            }
            2 -> {
                dishPosition = position
            }
            else -> {
                chosenUnit = unitList[position]
                Log.i("test", chosenUnit)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.add_products_to_dish, container, false)

        /** initialize ui with viewmodel*/
        viewModel = ViewModelProvider(this).get(AddViewModel::class.java)
        val thisViewModel = viewModel as AddViewModel

        /** Get the data about unit settings from shared preferences.
         * true means that user uses certain units.
         * metricAsBoolean is set as true because something needs to be chosen in order for app to work.*/
        val sharedPreferences = SharedPreferences(requireContext())
        metricAsBoolean = sharedPreferences.getValueBoolean("metric", true)
        usaAsBoolean = sharedPreferences.getValueBoolean("usa", false)

        /** binders*/
        val weightOfAddedProduct = view.findViewById<EditText>(R.id.product_weight_in_half_product)
        val addProductToDishBtn = view.findViewById<ImageButton>(R.id.add_product_to_halfproduct_btn)
        val productSpinner = view.findViewById<Spinner>(R.id.mySpinner)
        val dishSpinner = view.findViewById<Spinner>(R.id.dishSpinner)
        unitSpinner = view.findViewById<Spinner>(R.id.unitSpinner)

        /** ADAPTERs FOR SPINNERs */
        val productList = mutableListOf<String>()
        thisViewModel.readAllProductData.observe(
            viewLifecycleOwner,
            Observer { it.forEach { product -> productList.add(product.name) } })
        val productAdapter = ArrayAdapter(requireActivity(), R.layout.spinner_layout, productList)
        with(productSpinner)
        {
            adapter = productAdapter
            setSelection(0, false)
            onItemSelectedListener = this@AddProductToDish
            prompt = "Select product"
            gravity = Gravity.CENTER
            id = PRODUCT_SPINNER_ID
        }
        productAdapter.notifyDataSetChanged()

        val dishList = mutableListOf<String>()
        (viewModel as AddViewModel).readAllDishData.observe(
            viewLifecycleOwner,
            Observer { it.forEach { dish -> dishList.add(dish.name) } })
        val dishesAdapter = ArrayAdapter(requireActivity(), R.layout.spinner_layout, dishList)
        with(dishSpinner) {
            adapter = dishesAdapter
            setSelection(0, false)
            onItemSelectedListener = this@AddProductToDish
            prompt = "Select dish"
            gravity = Gravity.CENTER
            id = DISH_SPINNER_ID
        }
        dishesAdapter.notifyDataSetChanged()

        unitAdapter =
            ArrayAdapter(requireActivity(), R.layout.support_simple_spinner_dropdown_item, unitList)
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        with(unitSpinner) {
            adapter = unitAdapter
            setSelection(0, false)
            onItemSelectedListener = this@AddProductToDish
            prompt = "Select unit"
            gravity = Gravity.CENTER
            id = UNIT_SPINNER_ID
        }

        /**OBSERVING 'LIVEDATA' FROM ADDVIEWMODEL
         *  WHICH OBSERVES 'LIVEDATA' IN REPOSITORY
         *  WHICH OBSERVES 'LIVEDATA' FROM DAO*/

        thisViewModel.readAllProductData.observe(viewLifecycleOwner, Observer { products ->
            productAdapter.clear()
            products.forEach { product ->
                productAdapter.add(product.name)
                productAdapter.notifyDataSetChanged()
            }
        })

        thisViewModel.readAllDishData.observe(viewLifecycleOwner, Observer { dishes ->
            dishesAdapter.clear()
            dishes.forEach { dish ->
                dishesAdapter.add(dish.name)
                dishesAdapter.notifyDataSetChanged()
            }
        })

        /** BUTTON LOGIC*/
        addProductToDishBtn.setOnClickListener {
            if (weightOfAddedProduct.text.isNullOrEmpty()) {
                showToast(message = "You can't add product without weight.")
            } else {
                val chosenDish = thisViewModel.readAllDishData.value?.get(dishPosition!!)
                val chosenProduct = thisViewModel.readAllProductData.value?.get(productPosition!!)
                val weight = weightOfAddedProduct.text.toString().toDouble()
                thisViewModel.addProductToDish(
                    ProductIncluded(
                        0,
                        chosenProduct!!,
                        chosenDish!!.dishId,
                        chosenDish,
                        chosenProduct.productId,
                        weight,
                        chosenUnit
                    )
                )
            }
            weightOfAddedProduct.text.clear()
            Toast.makeText(
                requireContext(),
                "${thisViewModel.readAllProductData.value?.get(productPosition!!)?.name} added.",
                Toast.LENGTH_SHORT
            ).show()
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


