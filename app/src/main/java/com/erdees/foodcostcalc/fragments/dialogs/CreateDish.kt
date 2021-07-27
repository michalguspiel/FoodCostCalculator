package com.erdees.foodcostcalc.fragments.dialogs

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.erdees.foodcostcalc.Constants
import com.erdees.foodcostcalc.MainActivity
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.SharedFunctions.hideKeyboard
import com.erdees.foodcostcalc.SharedFunctions.makeSnackBar
import com.erdees.foodcostcalc.SharedPreferences
import com.erdees.foodcostcalc.model.Dish
import com.erdees.foodcostcalc.viewmodel.CreateDishViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics


class CreateDish(val parentView: View) : DialogFragment() {

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private lateinit var thisView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        thisView = inflater.inflate(R.layout.fragment_create_dish, container, false)

        /** initialize ui with viewmodel*/
        val viewModel = ViewModelProvider(this).get(CreateDishViewModel::class.java)

        /** binders*/
        val addDishBtn = thisView.findViewById<Button>(R.id.add_button_dialog)
        val dishName = thisView.findViewById<TextView>(R.id.new_dish_edittext)
        dishName.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) thisView.hideKeyboard()
        }

        val sharedPreferences = SharedPreferences(requireContext())

        /** BUTTON LOGIC*/
        addDishBtn.setOnClickListener {
            val marginAsString = sharedPreferences.getValueString("margin")
            val taxAsString = sharedPreferences.getValueString("tax")
            var tax = 23.0
            var margin = 100.0
            if (!taxAsString.isNullOrEmpty()) tax = taxAsString.toDouble()
            if (!marginAsString.isNullOrEmpty()) margin = marginAsString.toDouble()

            if (dishName.text.isNotEmpty()) {
                val dish = Dish(0, dishName.text.toString(), margin, tax)
                viewModel.addDish(dish)
                sendDataAboutDishCreated(dish)
                parentView.makeSnackBar(dish.name,requireContext())
                this.dismiss()

            } else Toast.makeText(activity, "Can't make nameless dish!", Toast.LENGTH_SHORT).show()
        }


        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return thisView
    }



    private fun sendDataAboutDishCreated(dish: Dish) {
        val thisDishBundle = Bundle()
        thisDishBundle.putString(Constants.DISH_NAME, dish.name)
        firebaseAnalytics.logEvent(Constants.DISH_CREATED, thisDishBundle)

    }

    companion object {
        const val TAG = "CreateDish"
    }


}
