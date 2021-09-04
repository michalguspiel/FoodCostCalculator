package com.erdees.foodcostcalc.ui.fragments.dishesFragment.editDishDialogFragment


import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.ui.dialogFragments.areYouSureFragment.AreYouSure
import com.erdees.foodcostcalc.ui.fragments.dishesFragment.models.DishModel
import com.erdees.foodcostcalc.ui.fragments.dishesFragment.models.GrandDishModel
import com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.models.HalfProductIncludedInDishModel
import com.erdees.foodcostcalc.ui.fragments.productsFragment.models.ProductIncluded
import com.erdees.foodcostcalc.viewmodel.adaptersViewModel.EditDishAdapterViewModel

class EditDishFragmentRecyclerAdapter(
    private val viewModel: EditDishAdapterViewModel,
    private val fragmentManager: FragmentManager,
    private val grandDishModel: GrandDishModel
) : RecyclerView.Adapter<EditDishFragmentRecyclerAdapter.EditDishViewHolder>() {


    /**Clones of main lists which populates an adapter
     * created in order to change this list with each holder edit text field
     * and with save button override original lists with this ones.
     *
     * It works like this so its possible to change weight of each product at once.*/
    var cloneOfListOfProductsIncluded: MutableList<ProductIncluded> = mutableListOf()
    var cloneOfListOfHalfProductModels: MutableList<HalfProductIncludedInDishModel> =
        mutableListOf()

    class EditDishViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.edit_dish_product_name)
        val unitTextView: TextView = view.findViewById(R.id.unit)
        val editTextView: EditText = view.findViewById(R.id.product_weight_edittext)
        val deleteProductBtn: ImageButton = view.findViewById(R.id.delete_product_in_dish_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditDishViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.small_list_of_products_v2, parent, false)
        cloneOfListOfProductsIncluded = grandDishModel.productsIncluded.toMutableList()
        cloneOfListOfHalfProductModels = grandDishModel.halfProductModels.toMutableList()

        return EditDishViewHolder(adapterLayout)
    }


    override fun getItemCount(): Int {
        return grandDishModel.halfProductModels.size + grandDishModel.productsIncluded.size
    }


    fun save(dishModel: DishModel) {
        viewModel.editDish(dishModel)
        notifyDataSetChanged()
        cloneOfListOfProductsIncluded.forEach { viewModel.editProductsIncluded(it) }
        cloneOfListOfHalfProductModels.forEach { viewModel.editHalfProductIncludedInDish(it) }
    }


    override fun onBindViewHolder(holder: EditDishViewHolder, position: Int) {
        @SuppressLint("SetTextI18n")
        fun setUnit(result: String, weight: Double) {
            if (weight <= 1) holder.unitTextView.text = result
            else holder.unitTextView.text = result + 's'
        }
        if (position < grandDishModel.productsIncluded.size) {
            holder.nameTextView.text =
                grandDishModel.productsIncluded[position].productModelIncluded.name // name of product not changeable
            holder.editTextView.setText(grandDishModel.productsIncluded[position].weight.toString()) // To set EditText with current data
            setUnit(
                grandDishModel.productsIncluded[position].weightUnit,
                grandDishModel.productsIncluded[position].weight
            )
            /**Holder for each delete product button */
            holder.deleteProductBtn.setOnClickListener {
                viewModel.setProductIncluded(grandDishModel.productsIncluded[position])
                AreYouSure().show(this.fragmentManager, "EditDishFragmentRecyclerAdapter")

            }

            /** Edit text product weight.
             *  When weight is changed the same position in cloneOfList gets changed.
             *  */

            holder.editTextView.addTextChangedListener((object : TextWatcher {
                override fun afterTextChanged(s: Editable) {

                }
                override fun beforeTextChanged(
                    s: CharSequence, start: Int,
                    count: Int, after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence, start: Int,
                    before: Int, count: Int
                ) {
                    if (!s.isNullOrBlank() && s.toString() != ".") {
                        if (position < cloneOfListOfProductsIncluded.size) cloneOfListOfProductsIncluded[position].weight =
                            s.toString().toDouble()
                    }

                }
            }
                    ))

        } else if (position >= grandDishModel.productsIncluded.size) {
            val thisPosition =
                position - grandDishModel.productsIncluded.size // to start counting position from new list
            holder.nameTextView.text =
                grandDishModel.halfProductModels[thisPosition].halfProductModel.name // name of product not changeable
            holder.editTextView.setText(grandDishModel.halfProductModels[thisPosition].weight.toString()) //To set EditText with current data
            Log.i(
                "from edit dishModel adapter",
                position.toString() + " " + grandDishModel.productsIncluded.size.toString()
            )
            setUnit(
                grandDishModel.halfProductModels[thisPosition].unit,
                grandDishModel.halfProductModels[thisPosition].weight
            )

            /**Holder for each delete half product button */
            holder.deleteProductBtn.setOnClickListener {
                viewModel.setHalfProductIncluded(grandDishModel.halfProductModels[thisPosition])
                AreYouSure().show(this.fragmentManager, "EditDishAdapterDeleteHalfProduct")
            }
            holder.editTextView.addTextChangedListener((object : TextWatcher {
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
                    if (!s.isNullOrBlank() && s.toString() != ".") {
                        if (thisPosition < cloneOfListOfHalfProductModels.size) {
                            cloneOfListOfHalfProductModels[thisPosition].weight =
                                s.toString().toDouble()
                        }
                    }
                }

            }))
        }
    }
}




