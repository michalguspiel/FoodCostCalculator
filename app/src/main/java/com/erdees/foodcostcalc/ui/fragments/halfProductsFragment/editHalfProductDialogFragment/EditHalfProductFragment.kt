package com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.editHalfProductDialogFragment

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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.erdees.foodcostcalc.*
import com.erdees.foodcostcalc.ui.dialogFragments.areYouSureFragment.AreYouSure
import com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.models.HalfProductModel
import com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.models.HalfProductWithProductsIncludedModel
import com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.models.ProductIncludedInHalfProductModel
import com.erdees.foodcostcalc.ui.fragments.settingsFragment.SharedPreferences
import com.erdees.foodcostcalc.utils.UnitsUtils.filterVol
import com.erdees.foodcostcalc.utils.UnitsUtils.filterWeight
import com.erdees.foodcostcalc.utils.Utils.getUnits
import com.erdees.foodcostcalc.viewmodel.adaptersViewModel.EditHalfProductAdapterViewModel

/**TODO REFACTORING INTO VIEW BINDING + MVVM PATTERN IMPROVEMENT */
class EditHalfProductFragment : DialogFragment(), AdapterView.OnItemClickListener {

    private var chosenUnit = ""
    private var unitList: MutableList<String> = mutableListOf()

    private lateinit var spinner: AutoCompleteTextView
    private lateinit var sharedPreferences: SharedPreferences
    private val spinnerId = 1


    override fun onResume() {
        unitList = getUnits(resources, sharedPreferences)
        Log.i(TAG, unitList.joinToString { it })
        unitList = when (halfProductPassedFromAdapterModel.halfProductModel.halfProductUnit) {
            "per piece" -> mutableListOf("per piece")
            "per kilogram" -> unitList.filterWeight()
            "per liter" -> unitList.filterVol()
            "per pound" -> unitList.filterWeight()
            "per gallon" -> unitList.filterVol()
            else -> mutableListOf("error!")
        }

        Log.i(TAG, unitList.joinToString { it })
        /**Spinner adapter*/
        val unitSpinnerAdapter =
            ArrayAdapter(requireContext(), R.layout.dropdown_item, unitList)
        with(spinner) {
            setAdapter(unitSpinnerAdapter)
            setText(halfProductPassedFromAdapterModel.halfProductModel.halfProductUnit, false)
            onItemClickListener = this@EditHalfProductFragment
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
        val viewModel = ViewModelProvider(this).get(EditHalfProductFragmentViewModel::class.java)

        /**Binders*/
        val name = view.findViewById<EditText>(R.id.edit_half_product_name)
        val saveBtn = view.findViewById<Button>(R.id.save_halfproduct_changes_button)
        val deleteBtn = view.findViewById<Button>(R.id.delete_dish_button)
        spinner = view.findViewById(R.id.edit_half_product_spinner)
        val recycleView =
            view.findViewById<RecyclerView>(R.id.recycler_view_products_in_half_product)
        val recyclerViewAdapter =
            EditHalfProductFragmentRecyclerAdapter(
                ViewModelProvider(this).get(
                    EditHalfProductAdapterViewModel::class.java
                ), childFragmentManager
            )
        recycleView.adapter = recyclerViewAdapter
        recycleView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        sharedPreferences = SharedPreferences(requireContext())


        /**Send data about which dishModel is being edited
         * so .setPosition(index of this dishModel in main list)*/
        viewModel.getHalfProducts().observe(viewLifecycleOwner, Observer { halfProduct ->
            viewModel.setPosition(halfProduct.indexOf(halfProductPassedFromAdapterModel.halfProductModel))
        })

        /** Observe data from viewmodel */
        viewModel.getHalfProductWithProductIncluded()
            .observe(viewLifecycleOwner, Observer {
                if (viewModel.getFlag().value == false) {
                    this.dismiss()
                    viewModel.setFlag(true)
                } else {
                    name.setText(halfProductPassedFromAdapterModel.halfProductModel.name)
                    viewModel
                        .getProductsIncludedFromHalfProduct(halfProductPassedFromAdapterModel.halfProductModel.halfProductId)
                        .observe(viewLifecycleOwner, Observer { eachProduct ->
                            val testData = mutableListOf<ProductIncludedInHalfProductModel>()
                            testData.addAll(eachProduct)
                            recyclerViewAdapter.switchLists(testData)
                        })
                }
            })

        /**Button logic*/

        saveBtn.setOnClickListener {
            if (chosenUnit == "") {
                chosenUnit = halfProductPassedFromAdapterModel.halfProductModel.halfProductUnit
            }
            recyclerViewAdapter.save(
                HalfProductModel(
                    halfProductPassedFromAdapterModel.halfProductModel.halfProductId,
                    name.text.toString(),
                    chosenUnit // chosen unit from spinner
                ), viewLifecycleOwner
            )
            this.dismiss()
        }

        deleteBtn.setOnClickListener {
          // TODO Just delete instead of asking // Test
            AreYouSure().show(childFragmentManager, TAG)
        }


        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        return view
    }

    companion object {
        fun newInstance(): EditHalfProductFragment =
            EditHalfProductFragment()

        const val TAG = "EditHalfProductFragment"
        lateinit var halfProductPassedFromAdapterModel: HalfProductWithProductsIncludedModel
    }


    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (parent?.id) {
            1 -> chosenUnit = unitList[position]
            else -> chosenUnit = unitList[position]
        }
    }
}
