package com.erdees.foodcostcalc.fragments.dialogs

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
import androidx.recyclerview.widget.RecyclerView
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.adapter.EditDishAdapter
import com.erdees.foodcostcalc.model.*
import com.erdees.foodcostcalc.viewmodel.AddViewModel
import com.erdees.foodcostcalc.viewmodel.EditDishViewModel
import com.erdees.foodcostcalc.viewmodel.HalfProductsViewModel
import com.erdees.foodcostcalc.viewmodel.adaptersViewModel.EditDishAdapterViewModel

class EditDish : DialogFragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_edit_dish, container, false)

        /** initialize ui with viewmodel*/
        val viewModel = ViewModelProvider(this).get(EditDishViewModel::class.java)

        val name = view.findViewById<EditText>(R.id.edit_dish_name)
        val marginEditText = view.findViewById<EditText>(R.id.edit_margin)
        val taxEditText = view.findViewById<EditText>(R.id.edit_dish_tax)


        /**Recycler adapter gets populated with GrandDish which
         * has two lists, list of products included and
         * HalfProductsIncludedInDish.
         * */
        val actualRecyclerView =
            view.findViewById<RecyclerView>(R.id.recycler_view_products_in_dish)
        val recyclerAdapter = EditDishAdapter(ViewModelProvider(this).get(EditDishAdapterViewModel::class.java), childFragmentManager, dishPassedFromAdapter)
        actualRecyclerView.adapter = recyclerAdapter
        val saveBtn = view.findViewById<Button>(R.id.save_halfproduct_changes_button)
        val deleteBtn = view.findViewById<Button>(R.id.delete_halfproduct_button)

        /**Send data about which dish is being edited
         * so .setPosition(index of this dish in main list)*/
        viewModel.getDishes().observe(viewLifecycleOwner, Observer { dish ->
            viewModel.setPosition(dish.indexOf(dishPassedFromAdapter.dish))
        })

        /** Observe data from viewmodel */
        viewModel.getGrandDishes().observe(viewLifecycleOwner, Observer {
            if (viewModel.getFlag().value == false) {
                viewModel.setFlag(true)
                this.dismiss()
            }
            name.setText(dishPassedFromAdapter.dish.name)
            marginEditText.setText(dishPassedFromAdapter.dish.marginPercent.toString())
            taxEditText.setText(dishPassedFromAdapter.dish.dishTax.toString())
        })



        /** BUTTON LOGIC*/

        saveBtn.setOnClickListener {
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
        //  lateinit var grandDishPassedFromAdapter: GrandDish
    }
}