package com.example.foodcostcalc.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.example.foodcostcalc.R
import com.example.foodcostcalc.fragments.Dishes
import com.example.foodcostcalc.fragments.Products
import com.example.foodcostcalc.fragments.dialogs.EditDish
import com.example.foodcostcalc.fragments.dialogs.EditProduct
import com.example.foodcostcalc.model.Dish
import com.example.foodcostcalc.model.DishWithProductsIncluded
import com.example.foodcostcalc.model.Product
import com.example.foodcostcalc.viewmodel.AddViewModel
import java.util.ArrayList


class RecyclerViewAdapter(val tag: String?, private val list: ArrayList<*>, private val fragmentManager: FragmentManager,val viewModel : AddViewModel)
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
                    EditDish.dishPassedFromAdapter = list[position] as DishWithProductsIncluded
                }
                else -> {
                }
            }


        }
    }


}