package com.example.foodcostcalc.fragments.dialogs

import android.annotation.SuppressLint
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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.foodcostcalc.MainActivity
import com.example.foodcostcalc.R
import com.example.foodcostcalc.SharedPreferences
import com.example.foodcostcalc.fragments.Add
import com.example.foodcostcalc.viewmodel.AddViewModel
import com.example.foodcostcalc.model.Product
import com.example.foodcostcalc.model.ProductIncluded
import io.reactivex.internal.operators.parallel.ParallelRunOn


class EditProduct : DialogFragment(), AdapterView.OnItemSelectedListener {

    var unitPosition: Int? = null
    var productId: Long? = null
    var unitList: Array<String> = arrayOf<String>()
    private lateinit var viewModel: AddViewModel

    @SuppressLint("ResourceType")
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_edit_product, container, false)

        /** initialize ui with viewmodel*/
        viewModel = ViewModelProvider(this).get(AddViewModel::class.java)


        /** empty list which gets populated by every 'ProductIncluded' that has the same
         * ID as edited product. */
        var productIncludedList = listOf<ProductIncluded>()

        val sharedPreferences = SharedPreferences(requireContext())

        /**Get units preferred by the user.*/
        fun getUnits(): Array<out String> {
            var chosenUnits = resources.getStringArray(R.array.piece)
            if (sharedPreferences.getValueBoolien("metric", false)) {
                chosenUnits += resources.getStringArray(R.array.addProductUnitsMetric)
            }
            if (sharedPreferences.getValueBoolien("usa", false)) {
                chosenUnits += resources.getStringArray(R.array.addProductUnitsUS)
            }
            unitList = chosenUnits
            return chosenUnits
        }



        /** Spinner */
        val unitSpinner = view.findViewById<Spinner>(R.id.spinner_edit_product)
        val unitList = resources.getStringArray(R.array.units)
        val unitsAdapter = ArrayAdapter(requireActivity(), R.layout.support_simple_spinner_dropdown_item,getUnits())
        with(unitSpinner) {
            adapter = unitsAdapter
            onItemSelectedListener = this@EditProduct
            gravity = Gravity.CENTER
            this.prompt = "Choose unit"
            id = 1
        }

        /** Binders*/
        val name = view.findViewById<EditText>(R.id.edit_product_name)
        val price = view.findViewById<EditText>(R.id.edit_product_price)
        val tax = view.findViewById<EditText>(R.id.edit_product_tax)
        val waste = view.findViewById<EditText>(R.id.edit_product_waste)
        val saveButton = view.findViewById<Button>(R.id.save_changes_button)
        val deleteButton = view.findViewById<Button>(R.id.delete_product_button)

        /**Send data about which dish is being edited
         * so .setPosition(index of this dish in main list)*/
        viewModel.getProducts().observe(viewLifecycleOwner, Observer { products ->
            viewModel.setPosition(products.indexOf(productPassedFromAdapter))
        })

        /** OBSERVE DATA FROM VIEWMODEL
         * Sets every text field value appropriate to edited product
         *
         * Flag works as a safety, if flag is set to false that means that
         * product is deleted and doesn't exist
         * so dialog is deleted and flag set to true so it will load information about
         * next chosen product
         *
         * Also gets information about every product included with same productID and
         * saves it as list so the product will be edited in every dish as well */


        viewModel.getProducts().observe(viewLifecycleOwner, Observer { product ->
            if (viewModel.getFlag().value == false) {
                this.dismiss()
                viewModel.setFlag(true)
            } else if (viewModel.getFlag().value == true) {
                productId = productPassedFromAdapter.productId                               // SAVES ID OF EDITED PRODUCT IN 'productID'
                name.setText(productPassedFromAdapter.name)
                price.setText(productPassedFromAdapter.pricePerUnit.toString())
                tax.setText(productPassedFromAdapter.tax.toString())
                waste.setText(productPassedFromAdapter.waste.toString())
                unitSpinner.setSelection(unitList.indexOf(productPassedFromAdapter.unit))

                viewModel.getCertainProductsIncluded(productId!!).                           // GETS LIST OF PRODUCT INCLUDED
                observe(viewLifecycleOwner, Observer { listOfProducts ->                     // WITH THE SAME ID AS productID
                    productIncludedList = listOfProducts                                     // AND SAVES IT IN 'productIncludedList'
                })


            }
        })


        /** BUTTON LOGIC*/
        saveButton.setOnClickListener {
            val productToChange = Product(productId!!,
                    name.text.toString(),
                    price.text.toString().toDouble(),
                    tax.text.toString().toDouble(),
                    waste.text.toString().toDouble(),
                    unitList[unitPosition!!])
            viewModel.editProduct(productToChange)

            productIncludedList.forEach {
                Log.i("test", it.dishOwnerId.toString() + it.productIncluded.name + " " + it.productIncluded.productId)
                viewModel.editProductsIncluded(ProductIncluded(it.productIncludedId,
                        productToChange,
                        it.dishOwnerId,
                        it.dish,
                        it.productOwnerId,
                        it.weight,
                        it.weightUnit)
                )
            }

            this.dismiss()
        }


        deleteButton.setOnClickListener {
            AreYouSure().show(childFragmentManager, TAG)

        }



        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        return view

    }


    companion object {
        fun newInstance(): EditProduct =
                EditProduct()

        const val TAG = "EditProduct"

        /**Position of Edited product */
        lateinit var productPassedFromAdapter: Product
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (parent?.id) {
            1 -> {
                unitPosition = position
            }
            else -> {
            }
        }

    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }
}