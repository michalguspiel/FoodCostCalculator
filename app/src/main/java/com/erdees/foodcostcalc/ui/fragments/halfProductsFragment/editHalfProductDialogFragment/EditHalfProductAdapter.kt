package com.erdees.foodcostcalc.ui.fragments.halfProductsFragment.editHalfProductDialogFragment

import android.annotation.SuppressLint
import android.app.Activity
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.erdees.foodcostcalc.databinding.EditDishProductRowBinding
import com.erdees.foodcostcalc.entities.HalfProductIncludedInDish
import com.erdees.foodcostcalc.entities.HalfProduct
import com.erdees.foodcostcalc.entities.ProductIncludedInHalfProduct
import com.erdees.foodcostcalc.utils.diffutils.ProductIncludedInHalfProductDiffUtil
import com.erdees.foodcostcalc.viewmodel.adaptersViewModel.EditHalfProductAdapterViewModel

class EditHalfProductAdapter(
  private val viewModel: EditHalfProductAdapterViewModel,
  private val activity: Activity
) :
  RecyclerView.Adapter<EditHalfProductAdapter.EditHalfProductHolder>() {

  /**List of ProductIncluded which this adapter works on,
   * initially empty,gets populated with method 'switchLists'
   * it works like this so after save button is hit
   * this list have the same ProductsIncluded as 'cloneOfList'*/
  var list: List<ProductIncludedInHalfProduct> = mutableListOf()

  /**List of same ProductsIncluded as a data which populates an adapter
   * created in order to change this list with each holder edit text field
   * and afterwards override original list with this one(with save btn)*/
  var cloneOfList: List<ProductIncludedInHalfProduct> = mutableListOf()

  fun switchLists(passedList: List<ProductIncludedInHalfProduct>) {
    val diffUtil = ProductIncludedInHalfProductDiffUtil(oldList = this.list, newList = passedList)
    val diffResult = DiffUtil.calculateDiff(diffUtil)
    this.list = passedList
    this.cloneOfList = passedList
    diffResult.dispatchUpdatesTo(this)
  }

  @SuppressLint("NotifyDataSetChanged")
  fun save(halfProduct: HalfProduct, viewLifecycleOwner: LifecycleOwner) {
    viewModel.editHalfProducts(halfProduct)
    cloneOfList.forEach { viewModel.editProductIncludedInHalfProduct(it) }

    /**So every Half product in dishModel is also edited.*/
    viewModel.getHalfProductsIncludedInDishFromDishByHalfProduct(halfProduct.halfProductId)
      .observe(
        viewLifecycleOwner
      ) { halfProductList ->
        halfProductList.forEach {
          viewModel.editHalfProductIncludedInDish(
            HalfProductIncludedInDish(
              it.halfProductIncludedInDishId,
              it.dish,
              it.dishOwnerId,
              halfProduct,
              halfProduct.halfProductId,
              it.weight,
              it.unit
            )
          )
        }
      }
  }

  class EditHalfProductHolder(val viewBinding: EditDishProductRowBinding) : RecyclerView.ViewHolder(viewBinding.root)

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditHalfProductHolder {
    return EditHalfProductHolder(EditDishProductRowBinding.inflate(
      LayoutInflater.from(activity),
      parent,
      false
    ))
  }

  override fun getItemCount(): Int {
    return list.size
  }

  override fun onBindViewHolder(
    holder: EditHalfProductHolder,
    position: Int
  ) {
    holder.viewBinding.productNameTextView.text =
      list[holder.adapterPosition].productIncluded.name // name of product not changeable
    holder.viewBinding.productWeightEdittext.setText(list[holder.adapterPosition].weight.toString()) // To set EditText with current data

    fun setUnit() {
      var result = list[holder.adapterPosition].weightUnit
      if (list[holder.adapterPosition].weight > 1) result += 's'
      holder.viewBinding.unitTextView.text = result
    }

    setUnit()

    /**Holder for each delete product button */
    holder.viewBinding.deleteProductInDishButton.setOnClickListener {
      viewModel.deleteProductIncludedInHalfProduct(list[holder.adapterPosition])
    }

    /** Edit text product weight.
     *  When weight is changed the same position in cloneOfList gets changed.
     *  */
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
        if (s.isNotEmpty() && s.toString() != ".") {
          if(holder.adapterPosition >= cloneOfList.size) return
          cloneOfList[holder.adapterPosition].weight = s.toString().toDouble()
        }
      }
    }))
  }
}
