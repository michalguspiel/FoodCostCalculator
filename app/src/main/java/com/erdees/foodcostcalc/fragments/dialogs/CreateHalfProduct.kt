package com.erdees.foodcostcalc.fragments.dialogs

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.SharedPreferences
import com.erdees.foodcostcalc.getUnits
import com.erdees.foodcostcalc.model.HalfProduct
import com.erdees.foodcostcalc.viewmodel.CreateDishViewModel
import com.erdees.foodcostcalc.viewmodel.CreateHalfProductViewModel
import com.erdees.foodcostcalc.viewmodel.HalfProductsViewModel




class CreateHalfProduct : DialogFragment(),AdapterView.OnItemSelectedListener {

    private var chosenUnit = ""
    private var unitList: MutableList<String> = mutableListOf()

    @SuppressLint("ResourceType")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_create_half_product,container,false)
        val viewModel = ViewModelProvider(this).get(CreateHalfProductViewModel::class.java)

        /** binders*/
        val addDishBtn = view.findViewById<Button>(R.id.add_button_dialog)
        val dishName = view.findViewById<TextView>(R.id.new_half_product_edittext)
        val unitSpinner = view.findViewById<Spinner>(R.id.unit_spinner_create_half_product)
        val sharedPreferences = SharedPreferences(requireContext())
        unitList = getUnits(resources,sharedPreferences)


        /**Adapter for unitSpinner*/
        val unitSpinnerAdapter = ArrayAdapter(requireActivity(), R.layout.support_simple_spinner_dropdown_item, unitList)
        with(unitSpinner){
            adapter = unitSpinnerAdapter
            setSelection(0,false)
            onItemSelectedListener = this@CreateHalfProduct
            gravity = Gravity.CENTER
            this.prompt = "Choose unit"
            id = 1
        }

        chosenUnit = unitList.first() //Without this line while dialog opened nothing is selected, no idea why setSelection above is not working... ill fix it later


        /** BUTTON LOGIC*/
        addDishBtn.setOnClickListener {

            if (dishName.text.isNotEmpty()) {
                val halfProduct = HalfProduct(0, dishName.text.toString(),chosenUnit)
                viewModel.addHalfProduct(halfProduct)
                this.dismiss()

            } else Toast.makeText(activity, "Can't make nameless half product!", Toast.LENGTH_SHORT).show()
        }

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        return view
    }
    companion object {
        fun newInstance(): CreateHalfProduct =
            CreateHalfProduct()

        const val TAG = "CreateHalfProduct"
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (parent?.id) {
            1 -> {
                chosenUnit = unitList[position]
                Log.i("test", chosenUnit)

            }
            else -> {
            }
        }

    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        Toast.makeText(requireContext(), "nothing selected", Toast.LENGTH_SHORT).show()
    }
}