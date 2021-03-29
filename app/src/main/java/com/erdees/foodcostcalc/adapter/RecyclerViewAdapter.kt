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
import com.erdees.foodcostcalc.formatPrice
import com.erdees.foodcostcalc.fragments.Dishes
import com.erdees.foodcostcalc.fragments.Products
import com.erdees.foodcostcalc.fragments.dialogs.EditDish
import com.erdees.foodcostcalc.fragments.dialogs.EditProduct
import com.erdees.foodcostcalc.model.GrandDish
import com.erdees.foodcostcalc.model.Product
import java.util.*


class RecyclerViewAdapter(val tag: String?, private val list: ArrayList<Product>, private val fragmentManager: FragmentManager)
    : RecyclerView.Adapter<RecyclerViewAdapter.RecyclerViewHolder>() {

    class RecyclerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.product_title)
        val nettoTextView: TextView = view.findViewById(R.id.product_netto_price)
        val foodcostTextView: TextView = view.findViewById(R.id.product_foodcost_price)
        val editButton: ImageButton = view.findViewById(R.id.edit_button)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val adapterLayout =  LayoutInflater.from(parent.context).inflate(R.layout.list_product, parent, false)
        return RecyclerViewHolder(adapterLayout)
    }

    override fun getItemCount(): Int {
        return list.size
    }


    @SuppressLint("WrongConstant", "ShowToast")
    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        holder.textView.text = list[position].name
        holder.nettoTextView.text = "Price ${list[position].unit} netto: ${formatPrice(list[position].pricePerUnit)}."
        holder.foodcostTextView.text = "Price ${list[position].unit} with foodcost: ${formatPrice(list[position].priceAfterWasteAndTax)}."
        holder.editButton.setOnClickListener {
                    EditProduct().show(fragmentManager, EditProduct.TAG)
                    EditProduct.productPassedFromAdapter = list[position] as Product
            }


        }
    }


