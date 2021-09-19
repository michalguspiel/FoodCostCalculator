package com.erdees.foodcostcalc.ui.fragments.dishesFragment.editDishDialogFragment

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.databinding.FragmentEditDishBinding
import com.erdees.foodcostcalc.ui.fragments.dishesFragment.models.GrandDishModel
import com.erdees.foodcostcalc.utils.ViewUtils.isNotEmptyNorJustDot

class EditDishFragment : DialogFragment() {

    private var _binding: FragmentEditDishBinding? = null
    private val binding get() = _binding!!
    private lateinit var recyclerFragmentRecyclerAdapter: EditDishFragmentRecyclerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditDishBinding.inflate(inflater, container, false)
        val view = binding.root
        val viewModel = ViewModelProvider(this).get(EditDishFragmentViewModel::class.java)

        viewModel.getGrandDishById(grandDishModelPassedFromAdapter.dishModel.dishId)
            .observe(viewLifecycleOwner, { grandDish ->
                setFields()
                recyclerFragmentRecyclerAdapter = EditDishFragmentRecyclerAdapter(
                    ViewModelProvider(this).get(EditDishAdapterViewModel::class.java),
                    requireActivity(),
                    grandDish
                )
                binding.recyclerViewProductsInDish.adapter = recyclerFragmentRecyclerAdapter
            })

        binding.saveDishChangesButton.setOnClickListener {
            if (allFieldsAreLegit()) {
                viewModel.saveDish(
                    grandDishModelPassedFromAdapter.dishModel.dishId,
                    binding.editDishName.text.toString(),
                    binding.editDishMargin.text.toString().toDouble(),
                    binding.editDishTax.text.toString().toDouble()
                )
                recyclerFragmentRecyclerAdapter.save()
                this.dismiss()
            } else Toast.makeText(
                requireContext(),
                getString(R.string.cannot_leave_empty_fields),
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.deleteDishButton.setOnClickListener {
            val alertDialog = AlertDialog.Builder(requireContext()).setTitle(R.string.are_you_sure)
                .setPositiveButton(getString(R.string.yes), null)
                .setNegativeButton(getString(R.string.back), null)
                .show()
            alertDialog.window?.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.background_for_dialogs
                )
            )
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                viewModel.deleteGrandDish(grandDishModelPassedFromAdapter)
                alertDialog.dismiss()
                this.dismiss()
            }
        }
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return view
    }

    private fun setFields() {
        binding.editDishName.setText(grandDishModelPassedFromAdapter.dishModel.name)
        binding.editDishMargin.setText(grandDishModelPassedFromAdapter.dishModel.marginPercent.toString())
        binding.editDishTax.setText(grandDishModelPassedFromAdapter.dishModel.dishTax.toString())
    }

    companion object {
        fun newInstance(): EditDishFragment =
            EditDishFragment()

        const val TAG = "EditDishFragment"
        lateinit var grandDishModelPassedFromAdapter: GrandDishModel
    }

    private fun allFieldsAreLegit(): Boolean {
        return (!binding.editDishName.text.isNullOrBlank() &&
                binding.editDishMargin.isNotEmptyNorJustDot() &&
                binding.editDishTax.isNotEmptyNorJustDot())
    }
}