package com.example.foodcostcalc.adapter


import android.media.Image
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
import com.example.foodcostcalc.fragments.AddViewModel
import com.example.foodcostcalc.fragments.dialogs.AreYouSure
import com.example.foodcostcalc.fragments.dialogs.EditDish
import com.example.foodcostcalc.model.Dish
import com.example.foodcostcalc.model.Product

class EditDishAdapter(val viewModel: AddViewModel, val fragmentManager: FragmentManager)
    : RecyclerView.Adapter<EditDishAdapter.EditDishViewHolder>() {

    var listOfPairs: MutableList<Pair<Product, Double>> = mutableListOf()
    /**List of same pairs as a data which populates an adapter
     * created in order to change this list with edittext
     * and afterwards override original list with this one(with save btn)*/
    var cloneOfList: MutableList<Pair<Product,Double>> = mutableListOf()


    class EditDishViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView          = view.findViewById(R.id.edit_dish_product_name)
        val unitTextView: TextView          = view.findViewById(R.id.unit)
        val editTextView: EditText          = view.findViewById(R.id.product_weight_edittext)
        val deleteProductBtn : ImageButton  = view.findViewById(R.id.delete_product_in_dish_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditDishViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
                .inflate(R.layout.small_list_of_products_v2, parent, false)
        return EditDishViewHolder(adapterLayout)
    }


    fun switchLists(list: MutableList<Pair<Product, Double>>){
        this.listOfPairs = list
        cloneOfList = listOfPairs
    notifyDataSetChanged()
    }

    override fun getItemCount():Int  {
        return listOfPairs.size
    }




    fun save(dish: Dish){
    viewModel.editDish(dish,cloneOfList)
        notifyDataSetChanged()

    }

    override fun onBindViewHolder(holder: EditDishViewHolder, position: Int) {
        holder.nameTextView.text = listOfPairs[position].first.name // name of product not changeable
        holder.unitTextView.text = listOfPairs[position].first.unit // unit, changeable in future
        holder.editTextView.setText(listOfPairs[position].second.toString()) // To set edittext with current data

        /**Holder for each delete product button */
        holder.deleteProductBtn.setOnClickListener{
            EditDish.position?.let { it1 -> viewModel.setPosition(it1) } // First Position as dish position in list
            viewModel.setSecondPosition(position)                        // Second position as product position in productsincluded list
            AreYouSure().show(this.fragmentManager,"EditDishAdapter")
        notifyDataSetChanged()
        }

         /** Edit text product weight CHANGEABLE observe data */
        holder.editTextView.addTextChangedListener((object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                if(s.isNotEmpty()) {
                    val cloned = cloneOfList[position].copy(second = s.toString().toDouble())
                    cloneOfList[position] = cloned
                }
                }
        }
                ))


    }

    }




