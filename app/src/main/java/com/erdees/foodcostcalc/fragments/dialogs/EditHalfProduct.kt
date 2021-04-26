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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.erdees.foodcostcalc.*
import com.erdees.foodcostcalc.SharedFunctions.filterVol
import com.erdees.foodcostcalc.SharedFunctions.filterWeight
import com.erdees.foodcostcalc.SharedFunctions.getUnits
import com.erdees.foodcostcalc.adapter.EditHalfProductAdapter
import com.erdees.foodcostcalc.model.HalfProduct
import com.erdees.foodcostcalc.model.HalfProductWithProductsIncluded
import com.erdees.foodcostcalc.model.ProductIncludedInHalfProduct
import com.erdees.foodcostcalc.viewmodel.AddViewModel
import com.erdees.foodcostcalc.viewmodel.EditHalfProductViewModel
import com.erdees.foodcostcalc.viewmodel.HalfProductsViewModel
import com.erdees.foodcostcalc.viewmodel.adaptersViewModel.EditHalfProductAdapterViewModel

class EditHalfProduct : DialogFragment(), AdapterView.OnItemClickListener {

    private var chosenUnit = ""
    private var unitList: MutableList<String> = mutableListOf()

    private lateinit var spinner: AutoCompleteTextView
    private lateinit var sharedPreferences: SharedPreferences
    private val spinnerId = 1


    override fun onResume() {
        unitList = getUnits(resources, sharedPreferences)
        Log.i(TAG,unitList.joinToString { it })
        unitList = when (halfProductPassedFromAdapter.halfProduct.halfProductUnit) {
            "per piece" -> mutableListOf("per piece")
            "per kilogram" -> unitList.filterWeight()
            "per liter" -> unitList.filterVol()
            "per pound" -> unitList.filterWeight()
            "per gallon" ->  unitList.filterVol()
            else -> mutableListOf("error!")
        }

        Log.i(TAG,unitList.joinToString { it })
        /**Spinner adapter*/
        val unitSpinnerAdapter =
            ArrayAdapter(requireContext(), R.layout.dropdown_item, unitList)
        with(spinner) {
            setAdapter(unitSpinnerAdapter)
            setText(halfProductPassedFromAdapter.halfProduct.halfProductUnit,false)
            onItemClickListener = this@EditHalfProduct
            id = spinnerId
            gravity = Gravity.CENTER
        }

        super.onResume()
    }

    @SuppressLint("ResourceType")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_edit_half_product, container, false)

        /** initialize ui with viewmodel*/
        val viewModel = ViewModelProvider(this).get(EditHalfProductViewModel::class.java)

        /**Binders*/
        val name = view.findViewById<EditText>(R.id.edit_half_product_name)
        val saveBtn = view.findViewById<Button>(R.id.save_halfproduct_changes_button)
        val deleteBtn = view.findViewById<Button>(R.id.delete_halfproduct_button)
        spinner = view.findViewById(R.id.edit_half_product_spinner)
        val recycleView =
            view.findViewById<RecyclerView>(R.id.recycler_view_products_in_half_product)
        val recyclerViewAdapter =
            EditHalfProductAdapter(ViewModelProvider(this).get(EditHalfProductAdapterViewModel::class.java), childFragmentManager)
        recycleView.adapter = recyclerViewAdapter
        sharedPreferences = SharedPreferences(requireContext())




        /**Send data about which dish is being edited
         * so .setPosition(index of this dish in main list)*/
        viewModel.getHalfProducts().observe(viewLifecycleOwner, Observer { halfProduct ->
            viewModel.setPosition(halfProduct.indexOf(halfProductPassedFromAdapter.halfProduct))
        })

        /** Observe data from viewmodel */
        viewModel.getHalfProductWithProductIncluded()
            .observe(viewLifecycleOwner, Observer {
                if (viewModel.getFlag().value == false) {
                    this.dismiss()
                    viewModel.setFlag(true)
                } else {
                    name.setText(halfProductPassedFromAdapter.halfProduct.name)
                    viewModel
                        .getProductsIncludedFromHalfProduct(halfProductPassedFromAdapter.halfProduct.halfProductId)
                        .observe(viewLifecycleOwner, Observer { eachProduct ->
                            val testData = mutableListOf<ProductIncludedInHalfProduct>()
                            testData.addAll(eachProduct)
                            recyclerViewAdapter.switchLists(testData)
                        })
                }
            })

        /**Button logic*/

        saveBtn.setOnClickListener {
            if(chosenUnit == "") { chosenUnit = halfProductPassedFromAdapter.halfProduct.halfProductUnit}
            recyclerViewAdapter.save(
                HalfProduct(
                    halfProductPassedFromAdapter.halfProduct.halfProductId,
                    name.text.toString(),
                    chosenUnit // chosen unit from spinner
                ), viewLifecycleOwner
            )
            this.dismiss()
        }

        deleteBtn.setOnClickListener {
            AreYouSure().show(childFragmentManager, TAG)
        }


        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        return view
    }

    companion object {
        fun newInstance(): EditHalfProduct =
            EditHalfProduct()

        const val TAG = "EditHalfProduct"
        lateinit var halfProductPassedFromAdapter: HalfProductWithProductsIncluded
    }


    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (parent?.id) {
            1 -> chosenUnit = unitList[position]
            else -> chosenUnit = unitList[position]
        }
    }
}