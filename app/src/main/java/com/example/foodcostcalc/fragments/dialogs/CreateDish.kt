package com.example.foodcostcalc.fragments.dialogs

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProviders
import com.example.foodcostcalc.InjectorUtils
import com.example.foodcostcalc.R
import com.example.foodcostcalc.fragments.AddViewModel
import com.example.foodcostcalc.model.Dish

class CreateDish : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        inflater.inflate(R.layout.fragment_create_dish, container, false)
        val view: View = inflater!!.inflate(R.layout.fragment_create_dish,container,false)
        @SuppressLint("WrongConstant")
        fun initializeUi() {
            val factory = InjectorUtils.provideAddViewModelFactory()
            val viewModel = ViewModelProviders.of(this, factory)
                .get(AddViewModel::class.java)

            /** BUTTON LOGIC*/
            val addDishBtn = view.findViewById<Button>(R.id.add_button_dialog)
            val dishName = view.findViewById<TextView>(R.id.new_dish_edittext)
            addDishBtn.setOnClickListener{
                if(dishName.text.isNotEmpty()) {
                    val dish = Dish(dishName.text.toString())
                    viewModel.addDishes(dish)
                    this.dismiss()
                }
                else Toast.makeText(activity,"Can't make nameless dish!",Toast.LENGTH_SHORT).show()
            }

        }
    initializeUi()
    return view



    }
    companion object {
        fun newInstance(): CreateDish =
            CreateDish()
        const val TAG = "CreateDish"
    }



}
