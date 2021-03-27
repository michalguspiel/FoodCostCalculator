package com.erdees.foodcostcalc.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.erdees.foodcostcalc.R
import com.erdees.foodcostcalc.fragments.Dishes
import com.erdees.foodcostcalc.fragments.Products
import com.erdees.foodcostcalc.fragments.dialogs.EditDish
import com.erdees.foodcostcalc.fragments.dialogs.EditProduct
import com.erdees.foodcostcalc.model.GrandDish
import com.erdees.foodcostcalc.model.Product
import java.util.*


class RecyclerViewAdapter(val tag: String?, private val list: ArrayList<*>, private val fragmentManager: FragmentManager)
    : RecyclerView.Adapter<RecyclerViewAdapter.RecyclerViewHolder>() {

    class RecyclerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.product_title)
        val editButton: ImageButton = view.findViewById(R.id.edit_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val adapterLayout = when (tag) {
            Products.TAG -> LayoutInflater.from(parent.context).inflate(R.layout.list_product, parent, false)
            Dishes.TAG -> LayoutInflater.from(parent.context).inflate(R.layout.list_product, parent, false)
            else -> LayoutInflater.from(parent.context).inflate(R.layout.small_list_products, parent, false)
        }
        return RecyclerViewHolder(adapterLayout)

    }

    override fun getItemCount(): Int {
        return list.size
    }


    @SuppressLint("WrongConstant", "ShowToast")
    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        holder.textView.text = list[position].toString()

        holder.editButton.setOnClickListener {
            when (this.tag) {
                Products.TAG -> {
                    EditProduct().show(fragmentManager, EditProduct.TAG)
                    EditProduct.productPassedFromAdapter = list[position] as Product
                }
                Dishes.TAG -> {
                    EditDish().show(fragmentManager, EditDish.TAG)
                    EditDish.dishPassedFromAdapter = list[position] as GrandDish
                }
                else -> {
                }
            }


        }
    }


}