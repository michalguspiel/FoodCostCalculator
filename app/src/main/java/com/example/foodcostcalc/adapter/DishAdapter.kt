package com.example.foodcostcalc.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.forEach
import androidx.core.view.get
import androidx.core.view.size
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodcostcalc.R
import com.example.foodcostcalc.fragments.dialogs.EditDish
import com.example.foodcostcalc.model.DishWithProductsIncluded
import com.example.foodcostcalc.viewmodel.AddViewModel
import java.util.ArrayList


class DishAdapter(val tag: String?,
                  private val list: ArrayList<DishWithProductsIncluded>,
                  private val fragmentManager: FragmentManager,
                  val viewModel : AddViewModel,
                val activity: Activity)
    : RecyclerView.Adapter<DishAdapter.RecyclerViewHolder>() {

    class RecyclerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dishNameTextView: TextView   = view.findViewById(R.id.dish_name_in_adapter)
        val dishMarginTextView :TextView = view.findViewById(R.id.dish_margin_in_adapter)
        val dishTaxTextView : TextView   = view.findViewById(R.id.dish_tax_in_adapter)
        val editButton: ImageButton      = view.findViewById(R.id.edit_button_in_dish_adapter)
        val listView: ListView           = view.findViewById(R.id.list_view)
        val totalPriceOfDish: TextView   = view.findViewById(R.id.total_price_dish_card_view)
        val finalPriceWithMarginAndTax:TextView   = view.findViewById(R.id.total_price_with_margin_dish_card_view)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context).inflate(R.layout.dish_card_view, parent, false)
        return RecyclerViewHolder(adapterLayout)

    }

    override fun getItemCount(): Int {
        return list.size
    }


    @SuppressLint("WrongConstant", "ShowToast", "SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        holder.dishNameTextView.text    = list[position].dish.name
        holder.dishMarginTextView.text  = "Margin: ${list[position].dish.marginPercent}%"
        holder.dishTaxTextView.text     = "Tax: ${list[position].dish.dishTax}%"
        holder.totalPriceOfDish.text    = list[position].formattedTotalPrice
        holder.finalPriceWithMarginAndTax.text = list[position].formattedPriceWithMarginAndTax
        holder.listView.adapter         = DishListViewAdapter(activity,list[position].productIncluded)

        holder.editButton.setOnClickListener {
            EditDish().show(fragmentManager, EditDish.TAG)
            EditDish.dishPassedFromAdapter = list[position]
        }
        fun getListSize(): Int {
            var result = 0
            for (eachProduct in list[position].productIncluded.indices){
                val listItem = holder.listView.adapter.getView(eachProduct,null,holder.listView)
                listItem.measure(0, View.MeasureSpec.UNSPECIFIED)
                result += listItem.measuredHeight
            }
           return result + (holder.listView.getDividerHeight() * (holder.listView.adapter.getCount() - 1))
        }
        holder.listView.layoutParams = LinearLayout.LayoutParams(holder.listView.layoutParams.width,getListSize())

        }
    }