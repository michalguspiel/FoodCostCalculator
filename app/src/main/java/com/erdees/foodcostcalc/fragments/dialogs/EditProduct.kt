package com.erdees.foodcostcalc.fragments.dialogs

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.erdees.foodcostcalc.*
import com.erdees.foodcostcalc.model.Product
import com.erdees.foodcostcalc.model.ProductIncluded
import com.erdees.foodcostcalc.model.ProductIncludedInHalfProduct
import com.erdees.foodcostcalc.viewmodel.AddViewModel
import com.erdees.foodcostcalc.viewmodel.EditProductViewModel
import com.erdees.foodcostcalc.viewmodel.HalfProductsViewModel


class EditProduct : DialogFragment(), AdapterView.OnItemClickListener {

    private var unitPosition: Int? = null
    private var productId: Long? = null
    private var unitList: MutableList<String> = mutableListOf()
    private lateinit var unitSpinner : AutoCompleteTextView
    private val spinnerId = 1
    private lateinit var viewModel: EditProductViewModel
    private lateinit var sharedPreferences : SharedPreferences

    override fun onResume() {
        unitList = getUnits(resources, sharedPreferences)
        unitList = when (productPassedFromAdapter.unit) {
            "per piece" -> mutableListOf("per piece")
            "per kilogram" -> unitList.filterWeight()
            "per liter" -> unitList.filterVol()
            "per pound" -> unitList.filterWeight()
            "per gallon" ->  unitList.filterVol()
            else -> mutableListOf("error!")
        }

        val unitsAdapter = ArrayAdapter(
            requireActivity(),
            R.layout.dropdown_item,
            unitList
        )
        with(unitSpinner) {
            setAdapter(unitsAdapter)
            onItemClickListener = this@EditProduct
            gravity = Gravity.CENTER
            id = spinnerId
        }

        super.onResume()
    }

    @SuppressLint("ResourceType")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_edit_product, container, false)

        /** initialize ui with viewmodel*/
        viewModel = ViewModelProvider(this).get(EditProductViewModel::class.java)

        sharedPreferences = SharedPreferences(requireContext())



        /** Spinner */
        unitSpinner = view.findViewById(R.id.spinner_edit_product)


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

        /** empty lists which gets populated by every 'ProductIncluded' and
         * 'ProductIncludedInHalfProduct' that has the same ID as edited product. */
        var productIncludedList = listOf<ProductIncluded>()
        var productIncludedInHalfProductList = listOf<ProductIncludedInHalfProduct>()

        /** OBSERVE DATA FROM VIEWMODEL
         * Sets every text field value appropriate to edited product
         *
         * Flag works as a safety, if flag is set to false that means that
         * product is deleted and doesn't exist
         * so dialog is deleted and flag set to true so it will load information about
         * next chosen product
         *
         * Also gets information about every product included with same productID and
         * saves it as list so the product will be edited in every dish as well
         *
         * All of this needs to be inside Observer because otherwise dialog isn't closed when
         * product is deleted.*/

        viewModel.getFlag().observe(viewLifecycleOwner, Observer { flag ->
            if (flag == false) {
                this.dismiss()
                viewModel.setFlag(true)
            } else if (flag == true) {
                productId = productPassedFromAdapter.productId                               // SAVES ID OF EDITED PRODUCT IN 'productID'
                name.setText(productPassedFromAdapter.name)
                price.setText(productPassedFromAdapter.pricePerUnit.toString())
                tax.setText(productPassedFromAdapter.tax.toString())
                waste.setText(productPassedFromAdapter.waste.toString())
                unitSpinner.setText(productPassedFromAdapter.unit,false)
                /**GET LIST OF PRODUCTS INCLUDED
                 * WITH THE SAME ID AS productID
                 * AND SAVE IT IN 'productIncludedList
                 * IT NEEDS TO BE HERE BECAUSE IF FLAG IS FALSE productId DOESN'T EXIST*/
                viewModel.getCertainProductsIncluded(productId!!).observe(
                    viewLifecycleOwner,
                    Observer { listOfProducts ->
                        productIncludedList = listOfProducts
                    })
                viewModel.getCertainProductsIncludedInHalfProduct(productId!!).observe(viewLifecycleOwner,
                Observer { listOfProducts ->
                    productIncludedInHalfProductList = listOfProducts
                })

            }
        })

        /** BUTTON LOGIC*/
        saveButton.setOnClickListener {
            if(unitPosition == null) unitPosition = unitList.indexOf(productPassedFromAdapter.unit)
            val productToChange = Product(
                productId!!,
                name.text.toString(),
                price.text.toString().toDouble(),
                tax.text.toString().toDouble(),
                waste.text.toString().toDouble(),
                unitList[unitPosition!!]
            )
            viewModel.editProduct(productToChange)

            productIncludedList.forEach {
                viewModel.editProductsIncluded(
                    ProductIncluded(
                        it.productIncludedId,
                        productToChange,
                        it.dishOwnerId,
                        it.dish,
                        it.productOwnerId,
                        it.weight,
                        it.weightUnit
                    )
                )
            }
            productIncludedInHalfProductList.forEach {
                viewModel.editProductIncludedInHalfProduct(
                    ProductIncludedInHalfProduct(
                        it.productIncludedInHalfProductId,
                        productToChange,
                        it.halfProduct,
                        it.halfProductHostId,
                        it.weight,
                        it.weightUnit,
                        it.weightOfPiece
                    )
                )
            }
            Thread.sleep(100) // This is here because otherwise dialog gets closed before all viewmodel functions are called
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


    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (parent?.id) {
            1 -> {
                unitPosition = position
            }
            else -> {
                unitPosition = position

            }
        }

    }
}