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
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.databinding.FragmentEditHalfProductBinding
import com.erdees.foodcostcalc.entities.HalfProduct
import com.erdees.foodcostcalc.ui.fragments.settingsFragment.SharedPreferences
import com.erdees.foodcostcalc.utils.UnitsUtils.filterVol
import com.erdees.foodcostcalc.utils.UnitsUtils.filterWeight
import com.erdees.foodcostcalc.utils.Utils.getUnits
import com.erdees.foodcostcalc.viewmodel.adaptersViewModel.EditHalfProductAdapterViewModel

class EditHalfProductFragment : DialogFragment(), AdapterView.OnItemClickListener {

  private var chosenUnit = ""
  private var unitList: MutableList<String> = mutableListOf()

  private lateinit var sharedPreferences: SharedPreferences
  private val spinnerId = 1

  private var _binding : FragmentEditHalfProductBinding? = null
  private val binding get() = _binding!!

  override fun onResume() {
    unitList = getUnits(resources, sharedPreferences)
    Log.i(TAG, unitList.joinToString { it })
//    unitList = when (halfProductPassedFromAdapter.halfProduct.halfProductUnit) {
//      "per piece" -> mutableListOf("per piece")
//      "per kilogram" -> unitList.filterWeight()
//      "per liter" -> unitList.filterVol()
//      "per pound" -> unitList.filterWeight()
//      "per gallon" -> unitList.filterVol()
//      else -> mutableListOf("error!")
//    }

    Log.i(TAG, unitList.joinToString { it })
    /**Spinner adapter*/
    val unitSpinnerAdapter =
      ArrayAdapter(requireContext(), R.layout.dropdown_item, unitList)
    with(binding.editHalfProductSpinner) {
      // todo fix it
//      setAdapter(unitSpinnerAdapter)
//      setText(halfProductPassedFromAdapter.halfProduct.halfProductUnit, false)
//      onItemClickListener = this@EditHalfProductFragment
//      id = spinnerId
//      gravity = Gravity.CENTER
    }

    super.onResume()
  }

  @SuppressLint("ResourceType")
  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = FragmentEditHalfProductBinding.inflate(inflater,container,false)
    /** initialize ui with viewmodel*/
    val viewModel = ViewModelProvider(this).get(EditHalfProductFragmentViewModel::class.java)
//    val recyclerViewAdapter =
//      EditHalfProductAdapter(
//        activity = requireActivity(), viewModel =
//        ViewModelProvider(this).get(
//          EditHalfProductAdapterViewModel::class.java
//        )
//      )
//    binding.recyclerViewProductsInHalfProduct.adapter = recyclerViewAdapter
    binding.recyclerViewProductsInHalfProduct.layoutManager =
      LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
    sharedPreferences = SharedPreferences(requireContext())

    /**Button logic*/
    binding.saveHalfproductChangesButton.setOnClickListener {
      // todo implement
//      if (chosenUnit.isEmpty()) {
//        chosenUnit = halfProductPassedFromAdapter.halfProduct.halfProductUnit
//      }
//      recyclerViewAdapter.save(
//        HalfProduct(
//          halfProductPassedFromAdapter.halfProduct.halfProductId,
//          binding.editHalfProductName.text.toString(),
//          chosenUnit // chosen unit from spinner
//        ), viewLifecycleOwner
//      )
      this.dismiss()
    }

    binding.deleteButton.setOnClickListener {
//      TODO
//      viewModel.deleteHalfProduct(halfProductPassedFromAdapter.halfProduct.halfProductId)
      this.dismiss()
    }

    dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

    return binding.root
  }

  companion object {
    fun newInstance(): EditHalfProductFragment =
      EditHalfProductFragment()

    const val TAG = "EditHalfProductFragment"
//    lateinit var halfProductPassedFromAdapter: HalfProductWithProductsIncluded
  }

  override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
    chosenUnit = when (parent?.id) {
      1 -> unitList[position]
      else -> unitList[position]
    }
  }
}
