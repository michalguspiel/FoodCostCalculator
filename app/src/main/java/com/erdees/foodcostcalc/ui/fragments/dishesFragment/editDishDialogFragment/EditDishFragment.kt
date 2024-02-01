package com.erdees.foodcostcalc.ui.fragments.dishesFragment.editDishDialogFragment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.databinding.FragmentEditDishBinding
import com.erdees.foodcostcalc.domain.model.dish.GrandDish
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

        viewModel.getGrandDishById(grandDishPassedFromAdapter.dish.dishId)
            .observe(viewLifecycleOwner) { grandDish ->
              setFields()
              recyclerFragmentRecyclerAdapter = EditDishFragmentRecyclerAdapter(
                ViewModelProvider(this).get(EditDishAdapterViewModel::class.java),
                requireActivity(),
                grandDish
              )
              binding.recyclerViewProductsInDish.adapter = recyclerFragmentRecyclerAdapter
            }

      binding.saveDishChangesButton.setOnClickListener {
            if (allFieldsAreLegit()) {
                viewModel.saveDish(
                    grandDishPassedFromAdapter.dish.dishId,
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
                viewModel.deleteGrandDish(grandDishPassedFromAdapter)
                this.dismiss()
        }
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return view
    }

    private fun setFields() {
        binding.editDishName.setText(grandDishPassedFromAdapter.dish.name)
        binding.editDishMargin.setText(grandDishPassedFromAdapter.dish.marginPercent.toString())
        binding.editDishTax.setText(grandDishPassedFromAdapter.dish.dishTax.toString())
    }

    companion object {
        fun newInstance(): EditDishFragment =
            EditDishFragment()

        const val TAG = "EditDishFragment"
        lateinit var grandDishPassedFromAdapter: GrandDish
    }

    private fun allFieldsAreLegit(): Boolean {
        return (!binding.editDishName.text.isNullOrBlank() &&
                binding.editDishMargin.isNotEmptyNorJustDot() &&
                binding.editDishTax.isNotEmptyNorJustDot())
    }
}
