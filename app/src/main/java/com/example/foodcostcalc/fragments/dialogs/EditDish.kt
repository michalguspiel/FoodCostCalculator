package com.example.foodcostcalc.fragments.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.example.foodcostcalc.InjectorUtils
import com.example.foodcostcalc.R
import com.example.foodcostcalc.adapter.EditDishAdapter
import com.example.foodcostcalc.fragments.AddViewModel
import com.example.foodcostcalc.model.Dish
import com.example.foodcostcalc.model.Product
import kotlin.properties.Delegates

class EditDish : DialogFragment(){

    lateinit var dish:Dish
    //var position by Delegates.notNull<Int>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        inflater.inflate(R.layout.fragment_edit_dish, container, false)
        val view: View = inflater!!.inflate(R.layout.fragment_edit_dish,container,false)

        fun initializeUi() {
            val factory = InjectorUtils.provideAddViewModelFactory()
            val viewModel = ViewModelProviders.of(this, factory)
                .get(AddViewModel::class.java)


            val name = view.findViewById<TextView>(R.id.edit_dish_name)


            /**Recycler view adapter made of list of pairs product+weight included in this dish */
            /** 28-01-21  at this point I came to realize that its better if data about weight of product
             * in the dish stays in dish as a collection of weights
             * every index of product included has its own index in weight collection
             * lets see wheres that gonna bring me
             * */
            val actualRecyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_products_in_dish)
            val recyclerAdapter = EditDishAdapter(viewModel,childFragmentManager)
            actualRecyclerView.adapter = recyclerAdapter
            viewModel.getDishes().observe(this, Observer { thisDish ->
                if(viewModel.getFlag().value == false){this.dismiss()
                    viewModel.setFlag(true)}
                else if(viewModel.getFlag().value == true) {
                    dish = thisDish[position!!]
                    var testData = mutableListOf<Pair<Product, Double>>()
                    name.text = thisDish[position!!].name
                    testData.addAll(thisDish[position!!].getPairs())
                    recyclerAdapter.switchLists(testData)
                }
                })




            /** BUTTON LOGIC*/

            val saveBtn = view.findViewById<Button>(R.id.save_dish_changes_button)
            val deleteBtn = view.findViewById<Button>(R.id.delete_dish_button)

            saveBtn.setOnClickListener{
                recyclerAdapter.save(dish)
                this.dismiss()
            }

            deleteBtn.setOnClickListener{
                AreYouSure().show(childFragmentManager,TAG)
                viewModel.setPosition(position!!)
               // viewModel.setFlag(false)
               // viewModel.deleteDish(dish)
            }

        }
        initializeUi()
        return view



    }
    companion object {
        fun newInstance(): EditDish =
            EditDish()
        const val TAG = "EditDish"
        var position: Int? = null

    }
}