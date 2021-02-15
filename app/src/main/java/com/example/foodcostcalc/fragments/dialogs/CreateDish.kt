package com.example.foodcostcalc.fragments.dialogs

import android.app.Application
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.foodcostcalc.R
import com.example.foodcostcalc.SharedPreferences
import com.example.foodcostcalc.viewmodel.AddViewModel
import com.example.foodcostcalc.model.Dish

class CreateDish : DialogFragment() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_create_dish, container, false)

        /** initialize ui with viewmodel*/
        val viewModel = ViewModelProvider(this).get(AddViewModel::class.java)

        /** binders*/
        val addDishBtn = view.findViewById<Button>(R.id.add_button_dialog)
        val dishName = view.findViewById<TextView>(R.id.new_dish_edittext)

        val sharedPreferences = SharedPreferences(requireContext())

        /** BUTTON LOGIC*/
        addDishBtn.setOnClickListener {
            val marginAsString = sharedPreferences.getValueString("margin")
            val taxAsString = sharedPreferences.getValueString("tax")
            var tax = 23.0
            var margin = 100.0
            if(!taxAsString.isNullOrEmpty()) tax = taxAsString.toDouble()
            if(!marginAsString.isNullOrEmpty()) margin = marginAsString.toDouble()

            if (dishName.text.isNotEmpty()) {
                val dish = Dish(0, dishName.text.toString(),margin,tax)
                viewModel.addDishes(dish)
                this.dismiss()
            } else Toast.makeText(activity, "Can't make nameless dish!", Toast.LENGTH_SHORT).show()
        }


        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        return view


    }

    companion object {
        fun newInstance(): CreateDish =
                CreateDish()

        const val TAG = "CreateDish"
    }


}
