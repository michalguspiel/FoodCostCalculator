package com.erdees.foodcostcalc.ui.fragments.dishesFragment.editDishDialogFragment

import android.app.Activity
import android.app.AlertDialog
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.databinding.EditDishProductRowBinding
import com.erdees.foodcostcalc.domain.model.dish.GrandDishModel
import com.erdees.foodcostcalc.utils.Utils.isNotBlankNorJustDot

class EditDishFragmentRecyclerAdapter(
    private val viewModel: EditDishAdapterViewModel,
    private val activity: Activity,
    private val grandDishModel: GrandDishModel
) : RecyclerView.Adapter<EditDishFragmentRecyclerAdapter.EditDishViewHolder>() {

    class EditDishViewHolder(val viewBinding: EditDishProductRowBinding) :
        RecyclerView.ViewHolder(viewBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditDishViewHolder {
        viewModel.updateClonesOfLists(grandDishModel)
        return EditDishViewHolder(
            EditDishProductRowBinding.inflate(
                LayoutInflater.from(activity),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return grandDishModel.halfProducts.size + grandDishModel.productsIncluded.size
    }

    fun save() {
        viewModel.saveLists()
    }

    override fun onBindViewHolder(holder: EditDishViewHolder, position: Int) {
        if (isRowAProduct(holder)) {
            setFields(
                grandDishModel.productsIncluded[holder.adapterPosition].productModelIncluded.name,
                grandDishModel.productsIncluded[holder.adapterPosition].weight,
                grandDishModel.productsIncluded[holder.adapterPosition].weightUnit,
                holder
            )
            holder.viewBinding.deleteProductInDishButton.setOnClickListener {
                buildDeleteProductAlertDialog(holder)
            }
            addProductWeightTextChangedListener(holder)
        } else if (isRowHalfProduct(holder)) {
            val thisPosition =
                holder.adapterPosition - grandDishModel.productsIncluded.size // to start counting position from new list
            setFields(
                grandDishModel.halfProducts[thisPosition].halfProductModel.name,
                grandDishModel.halfProducts[thisPosition].weight,
                grandDishModel.halfProducts[thisPosition].unit,
                holder
            )
            holder.viewBinding.deleteProductInDishButton.setOnClickListener {
                buildDeleteHalfProductAlertDialog(thisPosition)
            }
            addHalfProductWeightTextChangedListener(holder, thisPosition)
        }
    }

    private fun addHalfProductWeightTextChangedListener(holder: EditDishViewHolder, pos: Int) {
        holder.viewBinding.productWeightEdittext.addTextChangedListener((object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                if (s.isNotBlankNorJustDot()) {
                    if (pos < viewModel.cloneOfListOfHalfProductModels.size) {
                        viewModel.cloneOfListOfHalfProductModels[pos].weight =
                            s.toString().toDouble()
                    }
                }
            }

        }))
    }

    private fun buildDeleteProductAlertDialog(holder: EditDishViewHolder) {
        val alertDialog =
            AlertDialog.Builder(activity).setTitle(activity.getString(R.string.are_you_sure))
                .setPositiveButton(R.string.yes, null)
                .setNegativeButton(R.string.back, null)
                .show()
        alertDialog.window?.setBackgroundDrawable(
            ContextCompat.getDrawable(
                activity,
                R.drawable.background_for_dialogs
            )
        )
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            viewModel.deleteProductIncluded(grandDishModel.productsIncluded[holder.adapterPosition])
            alertDialog.dismiss()
        }
    }

    private fun buildDeleteHalfProductAlertDialog(pos: Int) {
        val alertDialog =
            AlertDialog.Builder(activity).setTitle(activity.getString(R.string.are_you_sure))
                .setPositiveButton(R.string.yes, null)
                .setNegativeButton(R.string.back, null)
                .show()
        alertDialog.window?.setBackgroundDrawable(
            ContextCompat.getDrawable(
                activity,
                R.drawable.background_for_dialogs
            )
        )
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            viewModel.deleteHalfProductIncluded(grandDishModel.halfProducts[pos])
            alertDialog.dismiss()
        }
    }

    private fun setFields(name: String, weight: Double, unit: String, holder: EditDishViewHolder) {
        holder.viewBinding.productNameTextView.text = name
        holder.viewBinding.productWeightEdittext.setText(weight.toString())
        setUnit(unit, weight, holder)
    }

    private fun isRowHalfProduct(holder: EditDishViewHolder): Boolean =
        (holder.adapterPosition >= grandDishModel.productsIncluded.size)

    private fun isRowAProduct(holder: EditDishViewHolder): Boolean =
        (holder.adapterPosition < grandDishModel.productsIncluded.size)

    private fun addProductWeightTextChangedListener(holder: EditDishViewHolder) {
        holder.viewBinding.productWeightEdittext.addTextChangedListener((object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                if (s.isNotBlankNorJustDot()) {
                    if (holder.adapterPosition < viewModel.cloneOfListOfProductsIncluded.size) viewModel.cloneOfListOfProductsIncluded[holder.adapterPosition].weight =
                        s.toString().toDouble()
                }
            }
        }
                ))
    }

    private fun setUnit(
        result: String,
        weight: Double,
        holder: EditDishViewHolder
    ) {
        if (weight <= 1) holder.viewBinding.unitTextView.text = result
        else holder.viewBinding.unitTextView.text =
            activity.getString(R.string.string_plural, result)
    }
}
