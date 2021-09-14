package com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.createHalfProductDialogFragment

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
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.models.HalfProductModel
import com.erdees.foodcostcalc.ui.fragments.settingsFragment.SharedPreferences
import com.erdees.foodcostcalc.utils.Constants
import com.erdees.foodcostcalc.utils.Utils.getUnits
import com.erdees.foodcostcalc.utils.ViewUtils.hideKeyboard
import com.erdees.foodcostcalc.utils.ViewUtils.makeCreationConfirmationSnackBar
import com.google.firebase.analytics.FirebaseAnalytics

/**TODO REFACTORING INTO VIEW BINDING + MVVM PATTERN IMPROVEMENT */


class CreateHalfProductFragment(private val parentView: View) : DialogFragment(),
    AdapterView.OnItemClickListener {

    private lateinit var firebaseAnalytics: FirebaseAnalytics


    private var chosenUnit = ""
    private var unitList: MutableList<String> = mutableListOf()
    lateinit var sharedPreferences: SharedPreferences
    lateinit var unitSpinner: AutoCompleteTextView
    val spinnerId = 2
    override fun onResume() {
        unitList = getUnits(resources,sharedPreferences)
        val unitSpinnerAdapter = ArrayAdapter(requireActivity(), R.layout.dropdown_item, unitList)
        /**Adapter for unitSpinner*/
        with(unitSpinner){
            setAdapter(unitSpinnerAdapter)
            onItemClickListener = this@CreateHalfProductFragment
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
        val view = inflater.inflate(R.layout.fragment_create_half_product, container, false)
        val viewModel = ViewModelProvider(this).get(CreateHalfProductFragmentViewModel::class.java)

        /** binders*/
        val addDishBtn = view.findViewById<Button>(R.id.add_button_dialog)
        val halfProductName = view.findViewById<TextView>(R.id.new_half_product_edittext)
        halfProductName.setOnFocusChangeListener { _, hasFocus ->
            if(!hasFocus) view.hideKeyboard()
        }
        unitSpinner = view.findViewById(R.id.unit_spinner_create_half_product)
        sharedPreferences = SharedPreferences(requireContext())
        unitList = getUnits(resources,sharedPreferences)




        chosenUnit = unitList.first()


        /** BUTTON LOGIC*/
        addDishBtn.setOnClickListener {

            if (halfProductName.text.isNotEmpty()) {
                val halfProduct = HalfProductModel(0, halfProductName.text.toString(), chosenUnit)
                viewModel.addHalfProduct(halfProduct)
                sendDataAboutHalfProductCreated(halfProduct)
                parentView.makeCreationConfirmationSnackBar(halfProduct.name, requireContext())
                this.dismiss()

            } else Toast.makeText(activity, "Can't make nameless half product!", Toast.LENGTH_SHORT).show()
        }

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        return view
    }

    private fun sendDataAboutHalfProductCreated(halfProductModel: HalfProductModel) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.VALUE, halfProductModel.name)
        firebaseAnalytics.logEvent(Constants.HALF_PRODUCT_CREATED, bundle)

    }

    companion object {
        const val TAG = "CreateHalfProductFragment"
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