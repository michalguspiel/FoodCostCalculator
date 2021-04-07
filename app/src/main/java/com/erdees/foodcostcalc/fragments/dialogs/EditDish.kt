package com.erdees.foodcostcalc.fragments.dialogs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.adapter.EditDishAdapter
import com.erdees.foodcostcalc.model.*
import com.erdees.foodcostcalc.viewmodel.EditDishViewModel
import com.erdees.foodcostcalc.viewmodel.adaptersViewModel.EditDishAdapterViewModel

class EditDish : DialogFragment() {

    lateinit var recyclerAdapter : EditDishAdapter
    lateinit var name : EditText
    lateinit var marginEditText: EditText
    lateinit var taxEditText: EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_edit_dish, container, false)

        /** initialize ui with viewmodel*/
        val viewModel = ViewModelProvider(this).get(EditDishViewModel::class.java)

        name = view.findViewById(R.id.edit_dish_name)
        marginEditText = view.findViewById(R.id.edit_margin)
        taxEditText = view.findViewById(R.id.edit_dish_tax)


        /**Recycler adapter gets populated with GrandDish which
         * has two lists, list of products included and
         * HalfProductsIncludedInDish.
         * */
        val actualRecyclerView =
            view.findViewById<RecyclerView>(R.id.recycler_view_products_in_dish)
        val saveBtn = view.findViewById<Button>(R.id.save_halfproduct_changes_button)
        val deleteBtn = view.findViewById<Button>(R.id.delete_halfproduct_button)

        /**Send data about which dish is being edited
         * so .setPosition(index of this dish in main list)*/
        viewModel.getDishes().observe(viewLifecycleOwner,  { dish ->
            viewModel.setPosition(dish.indexOf(dishPassedFromAdapter.dish))
        })


        /**Observing grandDish based on dishId that was passed with [dishPassedFromAdapter]
         * if viewModel flag is false change it to true and close dialog, otherwise set all text fields properly and set recyclerview
         * based on data in this [GrandDish].*/
        viewModel.getGrandDishById(dishPassedFromAdapter.dish.dishId).observe(viewLifecycleOwner, Observer { grandDish ->
            if (viewModel.getFlag().value == false) {
                viewModel.setFlag(true)
                this.dismiss()
            }
            else {
            name.setText(dishPassedFromAdapter.dish.name)
            marginEditText.setText(dishPassedFromAdapter.dish.marginPercent.toString())
            taxEditText.setText(dishPassedFromAdapter.dish.dishTax.toString())
            recyclerAdapter = EditDishAdapter(ViewModelProvider(this).get(EditDishAdapterViewModel::class.java), childFragmentManager, grandDish)
            actualRecyclerView.adapter = recyclerAdapter
            }
        })



        /** BUTTON LOGIC*/


        saveBtn.setOnClickListener {
            if(allFieldsAreLegit()) {
                recyclerAdapter.save(
                    Dish(
                        dishPassedFromAdapter.dish.dishId,
                        name.text.toString(),
                        marginEditText.text.toString().toDouble(),
                        taxEditText.text.toString().toDouble()
                    )
                )
                this.dismiss()
            }
            else Toast.makeText(requireContext(),"Can't leave empty fields", Toast.LENGTH_SHORT).show()
            }

        deleteBtn.setOnClickListener {
            Log.i("test", viewModel.getPosition().value.toString())
            AreYouSure().show(childFragmentManager, TAG)
        }


        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        return view

    }

    companion object {
        fun newInstance(): EditDish =
            EditDish()
        const val TAG = "EditDish"
        lateinit var dishPassedFromAdapter: GrandDish
    }

    private fun allFieldsAreLegit():Boolean{
        return (!name.text.isNullOrBlank() &&
                !marginEditText.text.isNullOrBlank() && marginEditText.text.toString() != "." &&
                !taxEditText.text.isNullOrBlank() && taxEditText.text.toString() != ".")
    }
}