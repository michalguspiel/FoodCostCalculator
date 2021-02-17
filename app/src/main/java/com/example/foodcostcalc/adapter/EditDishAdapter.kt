package com.example.foodcostcalc.adapter


import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodcostcalc.R
import com.example.foodcostcalc.fragments.dialogs.AreYouSure
import com.example.foodcostcalc.model.Dish
import com.example.foodcostcalc.model.ProductIncluded
import com.example.foodcostcalc.viewmodel.AddViewModel

class EditDishAdapter(private val viewModel: AddViewModel, private val fragmentManager: FragmentManager)
    : RecyclerView.Adapter<EditDishAdapter.EditDishViewHolder>() {

    /**List of ProductIncluded which this adapter works on,
     * initially empty,gets populated with method 'switchLists'
     * it works like this so after save button is hit
     * this list have the same ProductsIncluded as 'cloneOfList'*/
    var list: MutableList<ProductIncluded> = mutableListOf()

    /**List of same ProductsIncluded as a data which populates an adapter
     * created in order to change this list with each holder edit text field
     * and afterwards override original list with this one(with save btn)*/
    var cloneOfList: MutableList<ProductIncluded> = mutableListOf()


    class EditDishViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView          = view.findViewById(R.id.edit_dish_product_name)
        val unitTextView: TextView          = view.findViewById(R.id.unit)
        val editTextView: EditText          = view.findViewById(R.id.product_weight_edittext)
        val deleteProductBtn: ImageButton   = view.findViewById(R.id.delete_product_in_dish_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditDishViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
                .inflate(R.layout.small_list_of_products_v2, parent, false)
        return EditDishViewHolder(adapterLayout)
    }


    fun switchLists(passedList: MutableList<ProductIncluded>) {
        this.list = passedList
        cloneOfList = passedList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return list.size
    }


    fun save(dish: Dish) {
        viewModel.editDish(dish)
        notifyDataSetChanged()
        cloneOfList.forEach { viewModel.editProductsIncluded(it) }
    }


    override fun onBindViewHolder(holder: EditDishViewHolder, position: Int) {
        holder.nameTextView.text = list[position].productIncluded.name // name of product not changeable
        holder.editTextView.setText(list[position].weight.toString()) // To set EditText with current data

        fun setUnit() {
            var result = list[position].weightUnit
            if (list[position].weight > 1) result += 's'
            holder.unitTextView.text = result
        }


        setUnit()


        /**Holder for each delete product button */
        holder.deleteProductBtn.setOnClickListener {
            viewModel.setProductIncluded(list[position])
            AreYouSure().show(this.fragmentManager, "EditDishAdapter")
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
                if (s.isNotEmpty()) {
                    cloneOfList[position].weight = s.toString().toDouble()
                }
            }
        }
                ))


    }
}




