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
import com.example.foodcostcalc.fragments.HalfProducts
import com.example.foodcostcalc.fragments.dialogs.AreYouSure
import com.example.foodcostcalc.model.Dish
import com.example.foodcostcalc.model.HalfProduct
import com.example.foodcostcalc.model.HalfProductIncludedInDish
import com.example.foodcostcalc.model.ProductIncluded
import com.example.foodcostcalc.viewmodel.AddViewModel
import com.example.foodcostcalc.viewmodel.HalfProductsViewModel

class EditDishAdapter(
    private val viewModel: AddViewModel,
    private val hpViewModel: HalfProductsViewModel,
    private val fragmentManager: FragmentManager
) : RecyclerView.Adapter<EditDishAdapter.EditDishViewHolder>() {

    /**List of ProductIncluded which this adapter works on,
     * initially empty,gets populated with method 'switchLists'
     * it works like this so after save button is hit
     * this list have the same ProductsIncluded as 'cloneOfList'*/
    var listOfProductsIncluded: MutableList<ProductIncluded> = mutableListOf()
    var listOfHalfProducts: MutableList<HalfProductIncludedInDish> = mutableListOf()


    /**List of same ProductsIncluded as a data which populates an adapter
     * created in order to change this list with each holder edit text field
     * and afterwards override original list with this one(with save btn)*/
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
        return EditDishViewHolder(adapterLayout)
    }


    fun switchLists(passedList: MutableList<ProductIncluded>) {
        this.listOfProductsIncluded = passedList
        cloneOfListOfProductsIncluded = passedList
        notifyDataSetChanged()
    }

    //repeating myself cause this function is called from dishes fragment which doesnt have acces to this list of halfproducts
    // thats why i cant make one function for both of those calls
    fun switchSecondList(passedList: MutableList<HalfProductIncludedInDish>) {
        this.listOfHalfProducts = passedList
        cloneOfListOfHalfProducts = passedList
        notifyDataSetChanged()
    }


    override fun getItemCount(): Int {
        return listOfProductsIncluded.size + listOfHalfProducts.size
    }


    fun save(dish: Dish) {
        viewModel.editDish(dish)
        notifyDataSetChanged()
        cloneOfListOfProductsIncluded.forEach { viewModel.editProductsIncluded(it) }
        cloneOfListOfHalfProducts.forEach { hpViewModel.editHalfProductIncludedInDish(it) }
    }


    override fun onBindViewHolder(holder: EditDishViewHolder, position: Int) {
        fun setUnit(result: String, weight: Double) {
            if (weight <= 1) holder.unitTextView.text = result
            else holder.unitTextView.text = result + 's'
        }

        if (position < listOfProductsIncluded.size) {
            holder.nameTextView.text =
                listOfProductsIncluded[position].productIncluded.name // name of product not changeable
            holder.editTextView.setText(listOfProductsIncluded[position].weight.toString()) // To set EditText with current data
            setUnit(
                listOfProductsIncluded[position].weightUnit,
                listOfProductsIncluded[position].weight
            )
            /**Holder for each delete product button */
            holder.deleteProductBtn.setOnClickListener {
                viewModel.setProductIncluded(listOfProductsIncluded[position])
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
                           if(position < cloneOfListOfProductsIncluded.size) cloneOfListOfProductsIncluded[position].weight = s.toString().toDouble()
                        }
                    }
                }
                        ))

            }
         else if (position >= this.listOfProductsIncluded.size) {
            var thisPosition = position - listOfProductsIncluded.size // to start counting position from new list
                holder.nameTextView.text =
                    listOfHalfProducts[thisPosition].halfProduct.name // name of product not changeable
                holder.editTextView.setText(listOfHalfProducts[thisPosition].weight.toString()) // To set EditText with current data
                setUnit(
                    listOfHalfProducts[thisPosition].unit,
                    listOfHalfProducts[thisPosition].weight
                )

                /**Holder for each delete half product button */
                holder.deleteProductBtn.setOnClickListener {
                    viewModel.setHalfProductIncluded(listOfHalfProducts[thisPosition])
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
                            } }
                    }
                }))
            }
        }
    }




