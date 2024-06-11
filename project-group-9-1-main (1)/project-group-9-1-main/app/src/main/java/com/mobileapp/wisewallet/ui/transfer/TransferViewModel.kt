package com.mobileapp.wisewallet.ui.transfer

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.mobileapp.wisewallet.database.WWDatabase

/**
 * View model for the Transfer fragment.
 *
 * @param applicationContext the application context for the WiseWallet application.
 *      Do not provide a fragment context; this will lead to memory leaks.
 */
class TransferViewModel(
    private val applicationContext: Context
) : ViewModel() {

    /**
     * A WiseWallet database object.
     */
    val database = WWDatabase.getInstance(applicationContext)

    /**
     * A Flow yielding Lists of Accounts from the database.
     */
    val accountsFlow = database.accountDao().listenAccounts()

    /**
     * A Flow yielding Lists of Budgets from the database.
     */
    val budgetsFlow = database.budgetDao().listenBudgets()

    companion object {
        /**
         * Factory for creating `TransferViewModel`s
         */
        val FACTORY = object : ViewModelProvider.Factory {

            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val applicationContext = checkNotNull(
                    extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                ).applicationContext

                @Suppress("UNCHECKED_CAST")
                return TransferViewModel(applicationContext) as T
            }
        }
    }
}