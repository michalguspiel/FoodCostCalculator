package com.erdees.foodcostcalc.fragments.dialogs

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.erdees.foodcostcalc.Constants
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.SharedPreferences
import com.erdees.foodcostcalc.SharedFunctions.getUnits
import com.erdees.foodcostcalc.model.HalfProduct
import com.erdees.foodcostcalc.viewmodel.CreateHalfProductViewModel
import com.google.firebase.analytics.FirebaseAnalytics


class CreateHalfProduct : DialogFragment(),AdapterView.OnItemClickListener {

    private lateinit var firebaseAnalytics: FirebaseAnalytics


    private var chosenUnit = ""
    private var unitList: MutableList<String> = mutableListOf()
    lateinit var sharedPreferences: SharedPreferences
    lateinit var unitSpinner : AutoCompleteTextView
    val spinnerId = 2
    override fun onResume() {
        unitList = getUnits(resources,sharedPreferences)
        val unitSpinnerAdapter = ArrayAdapter(requireActivity(), R.layout.dropdown_item, unitList)
        /**Adapter for unitSpinner*/
        with(unitSpinner){
            setAdapter(unitSpinnerAdapter)
            onItemClickListener = this@CreateHalfProduct
            gravity = Gravity.CENTER
            id = spinnerId
        }
        super.onResume()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())
    }

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
        unitSpinner = view.findViewById<AutoCompleteTextView>(R.id.unit_spinner_create_half_product)
        sharedPreferences = SharedPreferences(requireContext())
        unitList = getUnits(resources,sharedPreferences)




        chosenUnit = unitList.first()


        /** BUTTON LOGIC*/
        addDishBtn.setOnClickListener {

            if (dishName.text.isNotEmpty()) {
                val halfProduct = HalfProduct(0, dishName.text.toString(),chosenUnit)
                viewModel.addHalfProduct(halfProduct)
                sendDataAboutHalfProductCreated(halfProduct)
                this.dismiss()

            } else Toast.makeText(activity, "Can't make nameless half product!", Toast.LENGTH_SHORT).show()
        }

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        return view
    }

    private fun sendDataAboutHalfProductCreated(halfProduct: HalfProduct){
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.VALUE,halfProduct.name)
        firebaseAnalytics.logEvent(Constants.HALF_PRODUCT_CREATED,bundle)

    }

    companion object {
        fun newInstance(): CreateHalfProduct =
            CreateHalfProduct()

        const val TAG = "CreateHalfProduct"
    }


    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (parent?.id) {
            1 -> {
                chosenUnit = unitList[position]

            }
            else -> {
                chosenUnit = unitList[position]
            }
        }

    }


}