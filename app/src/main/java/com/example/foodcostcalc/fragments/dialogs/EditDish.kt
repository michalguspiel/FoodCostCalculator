package com.example.foodcostcalc.fragments.dialogs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.foodcostcalc.R
import com.example.foodcostcalc.adapter.EditDishAdapter
import com.example.foodcostcalc.model.ProductIncluded
import com.example.foodcostcalc.viewmodel.AddViewModel
import com.example.foodcostcalc.model.Dish
import org.w3c.dom.Text

class EditDish : DialogFragment() {

    lateinit var dish: Dish

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_edit_dish, container, false)

        /** initialize ui with viewmodel*/
        val viewModel = ViewModelProvider(this).get(AddViewModel::class.java)


        val name = view.findViewById<TextView>(R.id.edit_dish_name)
        val marginEditText = view.findViewById<TextView>(R.id.edit_margin)


        /**Recycler view adapter made of list of pairs product+weight included in this dish */
        /** 28-01-21  at this point I came to realize that its better if data about weight of product
         * in the dish stays in dish as a collection of weights
         * every index of product included has its own index in weight collection
         * lets see wheres that gonna bring me
         * */
        val actualRecyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_products_in_dish)
        val recyclerAdapter = EditDishAdapter(viewModel, childFragmentManager)
        actualRecyclerView.adapter = recyclerAdapter
        val saveBtn = view.findViewById<Button>(R.id.save_dish_changes_button)
        val deleteBtn = view.findViewById<Button>(R.id.delete_dish_button)

        /** Observe data from viewmodel */
        viewModel.getDishesWithProductsIncluded().observe(viewLifecycleOwner, Observer { thisDish ->
            if (viewModel.getFlag().value == false) {
                this.dismiss()
                viewModel.setFlag(true)
            } else if (viewModel.getFlag().value == true) {
                dish = thisDish[position!!].dish
                name.text = thisDish[position!!].dish.name
                marginEditText.text = thisDish[position!!].dish.marginPercent.toString()
                viewModel.getIngredientsFromDish(dish.dishId).observe(viewLifecycleOwner, Observer { eachProduct ->
                    val testData = mutableListOf<ProductIncluded>()
                    testData.addAll(eachProduct)
                    recyclerAdapter.switchLists(testData)
                })

            }
        })

        /** BUTTON LOGIC*/

        saveBtn.setOnClickListener {
            recyclerAdapter.save(Dish(dish.dishId, name.text.toString(), marginEditText.text.toString().toDouble()))
            this.dismiss()
        }

        deleteBtn.setOnClickListener {
            AreYouSure().show(childFragmentManager, TAG)
            viewModel.setPosition(position!!)

        }


        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        return view


    }

    companion object {
        fun newInstance(): EditDish =
                EditDish()

        const val TAG = "EditDish"
        var position: Int? = null

    }
}