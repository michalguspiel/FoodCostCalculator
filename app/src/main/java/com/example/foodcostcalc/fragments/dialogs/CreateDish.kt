package com.example.foodcostcalc.fragments.dialogs

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

        /** BUTTON LOGIC*/
        addDishBtn.setOnClickListener {
            if (dishName.text.isNotEmpty()) {
                val dish = Dish(0, dishName.text.toString())
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
