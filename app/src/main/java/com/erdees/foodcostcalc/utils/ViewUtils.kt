package com.erdees.foodcostcalc.utils

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ListView
import android.widget.ScrollView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.erdees.foodcostcalc.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar


object ViewUtils {

    fun ScrollView.scrollUp() {
        this.fullScroll(ScrollView.FOCUS_UP)
    }

    fun showShortToast(
        context: Context,
        message: String,
        duration: Int = Toast.LENGTH_SHORT
    ) {
        Toast.makeText(context, message, duration).show()
    }

    fun EditText.isNotEmptyNorJustDot(): Boolean {
        return this.text.isNotEmpty() && this.text.toString() != "."
    }

    fun EditText.isNotBlankNorJustDot(): Boolean {
        return this.text.isNotBlank() && this.text.toString() != "."

    }

    fun BottomNavigationView.uncheckAllItems() {
        menu.setGroupCheckable(0, true, false)
        for (i in 0 until menu.size()) {
            menu.getItem(i).isChecked = false
        }
        menu.setGroupCheckable(0, true, true)
    }

    fun View.makeCreationConfirmationSnackBar(name: String, context: Context) {
        val snackBar =
            Snackbar.make(
                this,
                context.getString(R.string.successful_creation_message, name),
                Snackbar.LENGTH_SHORT
            )
        snackBar.setAction(context.getString(R.string.okay)) { snackBar.dismiss() }
        snackBar.setActionTextColor(ContextCompat.getColor(context, R.color.orange_500))
            .show()
    }

    fun View.hideKeyboard() {
        (context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager?)?.hideSoftInputFromWindow(
            this.windowToken,
            0
        )
    }

    fun View.makeGone() {
        this.visibility = View.GONE
    }

    fun View.makeVisible() {
        this.visibility = View.VISIBLE
    }


    /**Computes height of listView based on each row height, includes dividers.*/
    fun getListSize(indicesOfBothLists: List<Int>, listView: ListView): Int {
        var result = 0
        for (eachProduct in indicesOfBothLists) {
            val listItem = listView.adapter.getView(eachProduct, null, listView)
            listItem.measure(0, View.MeasureSpec.UNSPECIFIED)
            result += listItem.measuredHeight
        }
        return result + (listView.dividerHeight * (listView.adapter.count - 1))
    }
}

