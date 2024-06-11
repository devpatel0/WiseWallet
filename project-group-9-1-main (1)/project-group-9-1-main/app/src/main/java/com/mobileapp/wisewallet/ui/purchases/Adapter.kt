/*
    This file creates the logic for creating an adapter for a RecyclerView and setting the data that is held
    in each card.
 */
package com.mobileapp.wisewallet.ui.purchases

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mobileapp.wisewallet.R

class Adapter(private val transactionlist: List<TransactionData>) : RecyclerView.Adapter<Adapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.items_list, parent, false)
        return MyViewHolder(itemView)
    }


    override fun getItemCount(): Int {
        return transactionlist.size
    }

    // sets the data for each card in the RecyclerView
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentTransaction = transactionlist[position]
        holder.vendor.text = currentTransaction.vendor
        holder.account.text = currentTransaction.account
        holder.accountBalance.text = "" //currentTransaction.accountBalance.toString()
        holder.transactionAmount.text = currentTransaction.transactionAmount.toString()
    }

    // Binds the views for each card to its respective card
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val vendor: TextView = itemView.findViewById(R.id.vendor)
        val account: TextView = itemView.findViewById(R.id.account)
        val accountBalance: TextView = itemView.findViewById(R.id.accountBalance)
        val transactionAmount: TextView = itemView.findViewById(R.id.transactionAmount)
    }
}