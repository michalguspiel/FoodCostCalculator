package com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.editHalfProductDialogFragment

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.domain.model.halfProduct.HalfProductIncludedInDishModel
import com.erdees.foodcostcalc.domain.model.halfProduct.HalfProductModel
import com.erdees.foodcostcalc.domain.model.halfProduct.ProductIncludedInHalfProduct
import com.erdees.foodcostcalc.viewmodel.adaptersViewModel.EditHalfProductAdapterViewModel

class EditHalfProductFragmentRecyclerAdapter(
  private val viewModel: EditHalfProductAdapterViewModel
) :
    RecyclerView.Adapter<EditHalfProductFragmentRecyclerAdapter.EditHalfProductHolder>() {

    /**List of ProductIncluded which this adapter works on,
     * initially empty,gets populated with method 'switchLists'
     * it works like this so after save button is hit
     * this list have the same ProductsIncluded as 'cloneOfList'*/
    var list: MutableList<ProductIncludedInHalfProduct> = mutableListOf()

    /**List of same ProductsIncluded as a data which populates an adapter
     * created in order to change this list with each holder edit text field
     * and afterwards override original list with this one(with save btn)*/
    var cloneOfList: MutableList<ProductIncludedInHalfProduct> = mutableListOf()

    fun switchLists(passedList: MutableList<ProductIncludedInHalfProduct>) {
        this.list = passedList
        cloneOfList = passedList
        notifyDataSetChanged()
    }


    fun save(halfProductModel: HalfProductModel, viewLifecycleOwner: LifecycleOwner) {
        viewModel.editHalfProducts(halfProductModel)
        notifyDataSetChanged()
        cloneOfList.forEach { viewModel.editProductIncludedInHalfProduct(it) }

        /**So every Half product in dishModel is also edited.*/
        viewModel.getHalfProductsIncludedInDishFromDishByHalfProduct(halfProductModel.halfProductId)
            .observe(viewLifecycleOwner
            ) { halfProductList ->
              halfProductList.forEach {
                viewModel.editHalfProductIncludedInDish(
                  HalfProductIncludedInDishModel(
                    it.halfProductIncludedInDishId,
                    it.dishModel,
                    it.dishOwnerId,
                    halfProductModel,
                    halfProductModel.halfProductId,
                    it.weight,
                    it.unit
                  )
                )
              }
            }

    }


    class EditHalfProductHolder(view: View):RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.product_name_text_view)
        val unitTextView: TextView = view.findViewById(R.id.unit_text_view)
        val editTextView: EditText = view.findViewById(R.id.product_weight_edittext)
        val deleteProductBtn: ImageButton = view.findViewById(R.id.delete_product_in_dish_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditHalfProductHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.edit_dish_product_row, parent, false)

        return EditHalfProductHolder(adapterLayout)
    }

    override fun getItemCount(): Int {
        return list.size
    }


    override fun onBindViewHolder(
        holder: EditHalfProductHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {
        holder.nameTextView.text =
            list[position].productModelIncluded.name // name of product not changeable
        holder.editTextView.setText(list[position].weight.toString()) // To set EditText with current data

        fun setUnit() {
            var result = list[position].weightUnit
            if (list[position].weight > 1) result += 's'
            holder.unitTextView.text = result
        }


        setUnit()


        /**Holder for each delete product button */
        holder.deleteProductBtn.setOnClickListener {
          viewModel.deleteProductIncludedInHalfProduct(list[position])
        }

        /** Edit text product weight.
         *  When weight is changed the same position in cloneOfList gets changed.
         *  */
        holder.editTextView.addTextChangedListener((object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                if (s.isNotEmpty() && s.toString() != ".") {
                    cloneOfList[position].weight = s.toString().toDouble()
                }
            }
        }
                ))
    }
}
