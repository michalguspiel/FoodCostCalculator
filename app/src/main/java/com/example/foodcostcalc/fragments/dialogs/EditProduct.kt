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
import com.example.foodcostcalc.R
import com.example.foodcostcalc.viewmodel.AddViewModel
import com.example.foodcostcalc.model.Product


class EditProduct :DialogFragment(), AdapterView.OnItemSelectedListener {

    var unitPosition: Int? = null


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

        /** position to keep track which product is being edited*/
        val adapterPosition = position!!


        /** Spinner */
        val unitSpinner = view.findViewById<Spinner>(R.id.spinner_edit_product)
        val unitList = resources.getStringArray(R.array.units)
        val unitsAdapter = ArrayAdapter(requireActivity(),R.layout.support_simple_spinner_dropdown_item,unitList)
        with(unitSpinner){
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

        /** OBSERVE DATA FROM VIEWMODEL
         * Flag works as a safety, if flag is set to false that means that delete operation is going
         * so this doesn't try to set EditText fields from product data that is about to get
         * deleted*/

        viewModel.getProducts().observe(viewLifecycleOwner, Observer { product ->
            if (viewModel.getFlag().value == false) {
                this.dismiss()
                viewModel.setFlag(true)
            } else if (viewModel.getFlag().value == true) {
                name.setText(product[adapterPosition].name)
                price.setText(product[adapterPosition].pricePerUnit.toString())
                tax.setText(product[adapterPosition].tax.toString())
                waste.setText(product[adapterPosition].waste.toString())
                unitSpinner.setSelection(unitList.indexOf(product[adapterPosition].unit))
            }
        })


        /** BUTTON LOGIC*/
        saveButton.setOnClickListener {
            viewModel.getProducts().observe(viewLifecycleOwner, Observer { product ->
                val productToEdit = Product(product[adapterPosition].productId,
                    name.text.toString(),
                    price.text.toString().toDouble(),
                    tax.text.toString().toDouble(),
                    waste.text.toString().toDouble(),
                unitList[unitPosition!!])
                viewModel.editProduct(productToEdit)
            })



            this.dismiss()
        }

        deleteButton.setOnClickListener {
            viewModel.setPosition(adapterPosition)
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
        var position: Int? = null
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when(parent?.id){
            1->  {  unitPosition = position
            }
            else -> {}
        }

    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }
}