package com.erdees.foodcostcalc.adapter


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
import com.erdees.foodcostcalc.fragments.dialogs.AreYouSure
import com.erdees.foodcostcalc.model.*
import com.erdees.foodcostcalc.viewmodel.AddViewModel
import com.erdees.foodcostcalc.viewmodel.HalfProductsViewModel
import com.erdees.foodcostcalc.viewmodel.adaptersViewModel.EditDishAdapterViewModel

class EditDishAdapter(
    private val viewModel: EditDishAdapterViewModel,
    private val fragmentManager: FragmentManager,
    private val grandDish: GrandDish
) : RecyclerView.Adapter<EditDishAdapter.EditDishViewHolder>() {


    /**Clones of main lists which populates an adapter
     * created in order to change this list with each holder edit text field
     * and with save button override original lists with this ones.
     *
     * It works like this so its possible to change weight of each product at once.*/
    var cloneOfListOfProductsIncluded: MutableList<ProductIncluded> = mutableListOf()
    var cloneOfListOfHalfProducts: MutableList<HalfProductIncludedInDish> = mutableListOf()

    class EditDishViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.edit_dish_product_name)
        val unitTextView: TextView = view.findViewById(R.id.unit)
        val editTextView: EditText = view.findViewById(R.id.product_weight_edittext)
        val deleteProductBtn: ImageButton = view.findViewById(R.id.delete_product_in_dish_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditDishViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.small_list_of_products_v2, parent, false)
        cloneOfListOfProductsIncluded = grandDish.productsIncluded.toMutableList()
        cloneOfListOfHalfProducts = grandDish.halfProducts.toMutableList()

        return EditDishViewHolder(adapterLayout)
    }



    override fun getItemCount(): Int {
        return grandDish.halfProducts.size + grandDish.productsIncluded.size
    }


    fun save(dish: Dish) {
        viewModel.editDish(dish)
        notifyDataSetChanged()
        cloneOfListOfProductsIncluded.forEach { viewModel.editProductsIncluded(it) }
        cloneOfListOfHalfProducts.forEach { viewModel.editHalfProductIncludedInDish(it) }
    }


    override fun onBindViewHolder(holder: EditDishViewHolder, position: Int) {
        fun setUnit(result: String, weight: Double) {
            if (weight <= 1) holder.unitTextView.text = result
            else holder.unitTextView.text = result + 's'
        }
        if (position < grandDish.productsIncluded.size) {
            holder.nameTextView.text =
                grandDish.productsIncluded[position].productIncluded.name // name of product not changeable
            holder.editTextView.setText(grandDish.productsIncluded[position].weight.toString()) // To set EditText with current data
            setUnit(
                grandDish.productsIncluded[position].weightUnit,
                grandDish.productsIncluded[position].weight
            )
            /**Holder for each delete product button */
            holder.deleteProductBtn.setOnClickListener {
                viewModel.setProductIncluded(grandDish.productsIncluded[position])
                AreYouSure().show(this.fragmentManager, "EditDishAdapter")
            }

            /** Edit text product weight.
             *  When weight is changed the same position in cloneOfList gets changed.
             *  */

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
                    if (s.isNotEmpty()) {
                        if (position < cloneOfListOfProductsIncluded.size) cloneOfListOfProductsIncluded[position].weight =
                            s.toString().toDouble()
                    }
                }
            }
                    ))

        }
        else if (position >= grandDish.productsIncluded.size) {
            val thisPosition =
                position - grandDish.productsIncluded.size // to start counting position from new list
            holder.nameTextView.text =
                grandDish.halfProducts[thisPosition].halfProduct.name // name of product not changeable
            holder.editTextView.setText(grandDish.halfProducts[thisPosition].weight.toString()) // To set EditText with current data
            Log.i("from edit dish adapter", position.toString() + " " + grandDish.productsIncluded.size.toString())
            setUnit(
                grandDish.halfProducts[thisPosition].unit,
                grandDish.halfProducts[thisPosition].weight
            )

            /**Holder for each delete half product button */
            holder.deleteProductBtn.setOnClickListener {
                viewModel.setHalfProductIncluded(grandDish.halfProducts[thisPosition])
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
                    if (s.isNotEmpty()) {
                        if (thisPosition < cloneOfListOfHalfProducts.size) {
                            cloneOfListOfHalfProducts[thisPosition].weight =
                                s.toString().toDouble()
                        }
                    }
                }
            }))
        }
    }
}




