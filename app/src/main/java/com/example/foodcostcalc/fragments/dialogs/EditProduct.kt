package com.example.foodcostcalc.fragments.dialogs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.example.foodcostcalc.R
import com.example.foodcostcalc.fragments.AddViewModel
import com.example.foodcostcalc.model.Product


class EditProduct :DialogFragment(){

    private lateinit var viewModel: AddViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_edit_product, container, false)

        /** initialize ui with viewmodel*/
        viewModel = ViewModelProvider(this).get(AddViewModel::class.java)

        /** flag, if changed means that product was erased*/
        val adapterPosition = position!!


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

        viewModel.getProducts().observe(this, Observer { product ->
            if (viewModel.getFlag().value == false) {
                this.dismiss()
                viewModel.setFlag(true)
            } else if (viewModel.getFlag().value == true) {
                name.setText(product[adapterPosition].name)
                price.setText(product[adapterPosition].pricePerUnit.toString())
                tax.setText(product[adapterPosition].tax.toString())
                waste.setText(product[adapterPosition].waste.toString())
            }
        })


        /** BUTTON LOGIC*/
        saveButton.setOnClickListener {
            viewModel.getProducts().observe(this, Observer { product ->
                val productToEdit = Product(product[adapterPosition].productId,
                    name.text.toString(),
                    price.text.toString().toDouble(),
                    tax.text.toString().toDouble(),
                    waste.text.toString().toDouble())
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
}