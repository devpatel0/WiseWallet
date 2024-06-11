package com.mobileapp.wisewallet.ui.transfer

import android.content.Context
import android.icu.text.NumberFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.mobileapp.wisewallet.database.Account
import com.mobileapp.wisewallet.databinding.VhTransferAccountBinding
import java.util.Locale

/**
 * Creates and builds Views for a list of Accounts.
 *
 * @deprecated
 */
class TransferSpinnerAdapter(context: Context, list: MutableList<Account>):
    ArrayAdapter<Account>(context, 0, list) {

    constructor(context: Context): this(context, mutableListOf())

    private val inflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getDropDownView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding: VhTransferAccountBinding = if (convertView != null) {
            try {
                VhTransferAccountBinding.bind(convertView)
            } catch (e: NullPointerException) {
                VhTransferAccountBinding.inflate(inflater, parent, false)
            }
        } else {
            VhTransferAccountBinding.inflate(inflater, parent, false)
        }
        val item = getItem(position) ?: return binding.root
        binding.name.text = item.name

        binding.balance.text = NumberFormat.getInstance(Locale.getDefault(), NumberFormat.CURRENCYSTYLE)
            .format(item.balance * 0.01)
        return binding.root
    }


}