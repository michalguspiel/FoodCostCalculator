package com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.createHalfProductDialogFragment

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
import com.erdees.foodcostcalc.databinding.FragmentCreateHalfProductBinding
import com.erdees.foodcostcalc.utils.Constants
import com.erdees.foodcostcalc.utils.Utils.getUnits
import com.erdees.foodcostcalc.utils.ViewUtils.hideKeyboard
import com.erdees.foodcostcalc.utils.ViewUtils.makeCreationConfirmationSnackBar
import com.erdees.foodcostcalc.utils.ViewUtils.showShortToast

class CreateHalfProductFragment(private val parentView: View) : DialogFragment(),
    AdapterView.OnItemClickListener {

    private var _binding: FragmentCreateHalfProductBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CreateHalfProductFragmentViewModel

    private var chosenUnit = ""
    private var unitList: MutableList<String> = mutableListOf()

    override fun onResume() {
        unitList = getUnits(resources, viewModel.sharedPreferences)
        setUnitSpinner()
        super.onResume()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(CreateHalfProductFragmentViewModel::class.java)
        viewModel.updateFirebase()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateHalfProductBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.newHalfProductEdittext.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) view.hideKeyboard()
        }
        unitList = getUnits(resources, viewModel.sharedPreferences)
        chosenUnit = unitList.first()

        binding.createHalfProductBtn.setOnClickListener {
            if (binding.newHalfProductEdittext.text?.isNotEmpty() == true) {
                viewModel.addHalfProduct(binding.newHalfProductEdittext.text.toString(), chosenUnit)
                parentView.makeCreationConfirmationSnackBar(
                    binding.newHalfProductEdittext.text.toString(),
                    requireContext()
                )
                this.dismiss()
            } else showShortToast(
                requireContext(),
                getString(R.string.cannot_make_nameless_half_product)
            )
        }
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return view
    }

    private fun setUnitSpinner() {
        val unitSpinnerAdapter = ArrayAdapter(requireActivity(), R.layout.dropdown_item, unitList)
        with(binding.unitSpinnerCreateHalfProduct) {
            setAdapter(unitSpinnerAdapter)
            onItemClickListener = this@CreateHalfProductFragment
            gravity = Gravity.CENTER
            id = Constants.UNIT_SPINNER_ID
        }
    }

    companion object {
        const val TAG = "CreateHalfProductFragment"
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        chosenUnit = when (parent?.id) {
            1 -> {
                unitList[position]
            }
            else -> {
                unitList[position]
            }
        }
    }
}