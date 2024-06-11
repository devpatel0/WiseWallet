/*
Creates a Spinner that includes checkboxes so that multiple spinner items can be selected.
checkedItems
 */
package com.mobileapp.wisewallet.ui.purchases

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.TextView
import com.mobileapp.wisewallet.R

class CustomSpinnerAdapter(context: Context, val items: List<String>) : ArrayAdapter<String>(context, R.layout.spinner_item, items) {

    val checkedItems = Array(items.size) { false }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    private fun createView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.spinner_item, parent, false)
        val checkBox: CheckBox = view.findViewById(R.id.checkbox)
        val textView: TextView = view.findViewById(R.id.textView)
        Log.d("Spinner", "Item $position created, status is " + checkedItems[position])

        textView.text = items[position]

        checkBox.tag = position

        checkBox.isChecked = checkedItems[position]

        checkBox.setOnCheckedChangeListener(null)
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            val tagPosition = checkBox.tag as Int
            checkedItems[tagPosition] = isChecked
            Log.d("Spinner", "$tagPosition is $isChecked")
        }

        return view
    }

}