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
import com.example.foodcostcalc.viewmodel.AddViewModel

@Suppress("NAME_SHADOWING")
class AreYouSure : DialogFragment(){

private lateinit var viewModel: AddViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       val view: View =  inflater.inflate(R.layout.fragment_dialog_are_you_sure,container,false)
        /** initialize ui with viewmodel*/
        viewModel = ViewModelProvider(this).get(AddViewModel::class.java)

             /** Binders*/
            val confirmBtn = view.findViewById<Button>(R.id.button_yes)
            val cancelBtn = view.findViewById<Button>(R.id.button_cancel)

            /**positions to delete
             * one position was enough untill implementation
             * of deleteProductFromDish where function needs position of dish
             * and position of product to delete from it*/
            var pos:Int? = null // first position
            var secondPos: Int? = null //  second position
            /**Observe data to set positions to provide parameters for delete methods */
            viewModel.getPosition().observe(this, Observer { position ->
              pos = position
            })
            viewModel.getSecondPosition().observe(viewLifecycleOwner, Observer { position ->
                secondPos = position
            })

        /**Button  logic tag informs this dialog from where it was open so it knows what action to proceed*/

            confirmBtn.setOnClickListener{
                viewModel.setFlag(false)
                    when(this.tag) {
                        EditProduct.TAG -> viewModel.getProducts().observe(viewLifecycleOwner, Observer { viewModel.deleteProduct(it[pos!!]) })
                            EditDish.TAG -> viewModel.getDishes().observe(viewLifecycleOwner, Observer { viewModel.deleteDish(it[pos!!]) })
                        "EditDishAdapter" -> viewModel.getDishesWithProductsIncluded()
                                .observe(viewLifecycleOwner, Observer { viewModel.deleteProductIncluded(viewModel.getProductIncluded().value!!)})
                              //  .observe(viewLifecycleOwner, Observer { viewModel.deleteProductIncluded(it[pos!!].productIncluded[secondPos!!]) })
                        else -> this.dismiss()
                    }


                this.dismiss()
                }


            cancelBtn.setOnClickListener{
                this.dismiss()
            }



        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return view
    }

    companion object {
        fun newInstance():AreYouSure =
            AreYouSure()
            const val TAG = "AreYouSure"

    }
}