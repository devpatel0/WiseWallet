package com.mobileapp.wisewallet.ui.home

import com.mobileapp.wisewallet.database.Account

data class AddState(
    val accounts: List<Account> = emptyList(),
    val bankName: String = "",
    val bankType: String = "",
    val bankBalance: Long = 0,
    val isAddingAccount: Boolean = false
)
