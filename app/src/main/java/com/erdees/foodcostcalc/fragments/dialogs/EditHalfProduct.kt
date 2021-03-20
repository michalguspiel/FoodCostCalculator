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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.erdees.foodcostcalc.*
import com.erdees.foodcostcalc.adapter.EditHalfProductAdapter
import com.erdees.foodcostcalc.model.HalfProduct
import com.erdees.foodcostcalc.model.HalfProductWithProductsIncluded
import com.erdees.foodcostcalc.model.ProductIncludedInHalfProduct
import com.erdees.foodcostcalc.viewmodel.AddViewModel
import com.erdees.foodcostcalc.viewmodel.HalfProductsViewModel

class EditHalfProduct : DialogFragment(), AdapterView.OnItemSelectedListener {

    private var chosenUnit = ""
    private var unitList: MutableList<String> = mutableListOf()


    @SuppressLint("ResourceType")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_edit_half_product, container, false)

        /** initialize ui with viewmodel*/
        val addViewModel = ViewModelProvider(this).get(AddViewModel::class.java)
        val halfProductViewModel = ViewModelProvider(this).get(HalfProductsViewModel::class.java)

        /**Binders*/
        val name = view.findViewById<EditText>(R.id.edit_half_product_name)
        val saveBtn = view.findViewById<Button>(R.id.save_halfproduct_changes_button)
        val deleteBtn = view.findViewById<Button>(R.id.delete_halfproduct_button)
        val spinner = view.findViewById<Spinner>(R.id.edit_half_product_spinner)
        val recycleView =
            view.findViewById<RecyclerView>(R.id.recycler_view_products_in_half_product)
        val recyclerViewAdapter =
            EditHalfProductAdapter(halfProductViewModel, addViewModel, childFragmentManager)
        recycleView.adapter = recyclerViewAdapter
        val sharedPreferences = SharedPreferences(requireContext())

        unitList = getUnits(resources, sharedPreferences)
        unitList = when (EditHalfProduct.halfProductPassedFromAdapter.halfProduct.halfProductUnit) {
            "per piece" -> mutableListOf("per piece")
            "per kilogram" -> unitList.filterWeight()
            "per liter" -> unitList.filterVol()
            "per pound" -> unitList.filterWeight()
            "per gallon" ->  unitList.filterVol()
            else -> mutableListOf("error!")

        }

        /**Spinner adapter*/
        val unitSpinnerAdapter =
            ArrayAdapter(requireContext(), R.layout.support_simple_spinner_dropdown_item, unitList)
        with(spinner) {
            adapter = unitSpinnerAdapter
            setSelection(unitList.indexOf(halfProductPassedFromAdapter.halfProduct.halfProductUnit))
            onItemSelectedListener = this@EditHalfProduct
            id = 1
            gravity = Gravity.CENTER
            prompt = "Unit"
        }

        /**Send data about which dish is being edited
         * so .setPosition(index of this dish in main list)*/
        halfProductViewModel.getHalfProducts().observe(viewLifecycleOwner, Observer { halfProduct ->
            addViewModel.setPosition(halfProduct.indexOf(halfProductPassedFromAdapter.halfProduct))
        })

        /** Observe data from viewmodel */
        halfProductViewModel.getHalfProductWithProductIncluded()
            .observe(viewLifecycleOwner, Observer {
                if (addViewModel.getFlag().value == false) {
                    this.dismiss()
                    addViewModel.setFlag(true)
                } else {
                    name.setText(halfProductPassedFromAdapter.halfProduct.name)
                    halfProductViewModel
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

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (parent?.id) {
            1 -> chosenUnit = unitList[position]
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        Toast.makeText(requireContext(), "nothing selected", Toast.LENGTH_LONG).show()
    }
}