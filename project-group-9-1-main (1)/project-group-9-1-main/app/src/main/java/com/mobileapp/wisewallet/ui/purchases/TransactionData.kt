/*
This file defines a transaction object that contains the core data needed to display recycler view cards
 */

package com.mobileapp.wisewallet.ui.purchases

data class TransactionData(
    val transactionAmount:Double,
    val accountBalance:Double,
    val vendor:String,
    val account:String
    ):java.io.Serializable
