package com.example.foodcostcalc.fragments.dialogs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.foodcostcalc.R
import com.example.foodcostcalc.model.Dish
import com.example.foodcostcalc.model.Product
import com.example.foodcostcalc.viewmodel.AddViewModel

@Suppress("NAME_SHADOWING")
class AreYouSure : DialogFragment() {

    private lateinit var viewModel: AddViewModel
    private lateinit var dishToDelete: Dish
    private lateinit var productToDelete: Product

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_dialog_are_you_sure, container, false)
        /** initialize ui with viewmodel*/
        viewModel = ViewModelProvider(this).get(AddViewModel::class.java)

        /** Binders*/
        val confirmBtn = view.findViewById<Button>(R.id.button_yes)
        val cancelBtn = view.findViewById<Button>(R.id.button_cancel)

        /**Position to delete, after pushing delete button, information about product/dish index in main list
         * is passed to this variable.*/
        var pos: Int? = null

        /**Observe data to set positions to provide parameters for delete methods */
        viewModel.getPosition().observe(viewLifecycleOwner, Observer { position ->
            pos = position
        })
        /** Get dish to delete only if this was called from EditDish. */
        viewModel.getDishes().observe(viewLifecycleOwner, Observer { dish ->
            if (this.tag == EditDish.TAG) dishToDelete = dish[pos!!]
        })
        /**Get product to delete only if this was called from EditProduct.*/
        viewModel.getProducts().observe(viewLifecycleOwner, Observer { product ->
            if (this.tag == EditProduct.TAG) productToDelete = product[pos!!]
        })

        /**Button logic: tag informs this dialog from where it was open so it knows what action to proceed.*/
        confirmBtn.setOnClickListener {
            viewModel.setFlag(false)
            when (this.tag) {
                EditProduct.TAG -> viewModel.deleteProduct(productToDelete)
                EditDish.TAG -> viewModel.deleteDish(dishToDelete)
                "EditDishAdapter" -> viewModel.getDishesWithProductsIncluded()
                        .observe(viewLifecycleOwner, Observer { viewModel.deleteProductIncluded(viewModel.getProductIncluded().value!!) })
                else -> this.dismiss()
            }
            this.dismiss()
        }


        cancelBtn.setOnClickListener {
            this.dismiss()
        }



        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return view
    }

    companion object {
        fun newInstance(): AreYouSure =
                AreYouSure()
        const val TAG = "AreYouSure"

    }
}