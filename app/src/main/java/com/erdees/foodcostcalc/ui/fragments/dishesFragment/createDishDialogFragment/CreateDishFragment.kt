package com.erdees.foodcostcalc.ui.fragments.dishesFragment.createDishDialogFragment

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
import com.erdees.foodcostcalc.databinding.FragmentCreateDishBinding
import com.erdees.foodcostcalc.utils.ViewUtils.hideKeyboard
import com.erdees.foodcostcalc.utils.ViewUtils.makeCreationConfirmationSnackBar
import com.google.firebase.analytics.FirebaseAnalytics

class CreateDishFragment(private val parentView: View) : DialogFragment() {

    private var _binding: FragmentCreateDishBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateDishBinding.inflate(inflater, container, false)
        val thisView = binding.root
        val viewModel = ViewModelProvider(this).get(CreateDishFragmentViewModel::class.java)

        binding.newDishEdittext.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) thisView.hideKeyboard()
        }

        binding.addButtonDialog.setOnClickListener {
            if (binding.newDishEdittext.text?.isNotEmpty() == true) {
                val dishCreated =
                    viewModel.addDish(binding.newDishEdittext.text.toString())
                parentView.makeCreationConfirmationSnackBar(dishCreated.name, requireContext())
                this.dismiss()
            } else Toast.makeText(
                activity,
                getString(R.string.cannot_make_nameless_dish),
                Toast.LENGTH_SHORT
            )
                .show()
        }
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return thisView
    }

    companion object {
        const val TAG = "CreateDishFragment"
    }
}
