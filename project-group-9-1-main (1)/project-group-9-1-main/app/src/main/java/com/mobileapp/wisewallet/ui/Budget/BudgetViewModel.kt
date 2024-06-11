// This file is a BudgetViewModel class and it is used to access and store information in a few of
// the databases that WiseWallet utilizes. This file specifically store or retrieves data that is
// needed by the budget fragment.
package com.mobileapp.wisewallet.ui.Budget

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.mobileapp.wisewallet.database.Budget
import com.mobileapp.wisewallet.database.Spent
import com.mobileapp.wisewallet.database.WWDatabase
import kotlinx.coroutines.launch

/**
 * View model for the Budget fragment.
 *
 * @param applicationContext the application context for the WiseWallet application.
 *      Do not provide a fragment context; this will lead to memory leaks.
 */

class BudgetViewModel(private val applicationContext: Context) : ViewModel() {
    /**
     * A WiseWallet database object.
     */
    private val database = WWDatabase.getInstance(applicationContext)
    private val budgetDao = database.budgetDao()
    private val spentDao = database.spentDao()
    private val transactionDao = database.transactionDao()

    // Retrieves and listens to these databases
    /**
     * A Flow yielding Lists of Budgets from the database.
     */
    val budgets = database.budgetDao().listenBudgets()
    /**
     * A Flow yielding Lists of Spent from the database.
     */
    val spent = database.spentDao().listenspents()
    /**
     * A Flow yielding Lists of Transactions from the database.
     */
    val transaction = database.transactionDao().listenTransactions()

    /**
     * Factory for creating `BudgetViewModel`s
     */
    companion object {
        val FACTORY = object : ViewModelProvider.Factory {

            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val applicationContext = checkNotNull(
                    extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                ).applicationContext

                @Suppress("UNCHECKED_CAST")
                return BudgetViewModel(applicationContext) as T
            }
        }
    }


    /**
     * Function to insert a Budget into the Budget Database
     *
     * @param budget Budget object
     *
     */
    fun insertBudget(budget: Budget) {
        viewModelScope.launch {
            budgetDao.insert(budget)
            Log.d("BudgetInsert", "Budget inserted successfully: $budget") }
    }

    /**
     * Function to insert a Spent into the Spent Database
     *
     * @param spent Spent object
     *
     */
    fun insertSpent(spent: Spent) {
        viewModelScope.launch {
            spentDao.insert(spent)
            Log.d("SpentInsert", "Spent inserted successfully: $spent") }
    }

    /**
     * Function to update a Budget into the Budget Database
     *
     * @param budget Budget object
     *
     */
    fun updateBudget(budget: Budget) {
        viewModelScope.launch {
            budgetDao.update(budget)
            Log.d("BudgetUpdate", "Budget updated successfully: $budget") }
    }

    /**
     * Function to update a Spent into the Spent Database
     *
     * @param spent Spent object
     *
     */
    fun updateSpent(spent: Spent) {
        viewModelScope.launch {
            spentDao.update(spent)
            Log.d("SpentUpdated", "Spent updated successfully: $spent") }
    }

    /**
     * Function to delete all the Budgets in the Budget Database
     *
     */
    fun deleteAllBudgets() {
        viewModelScope.launch {
            budgetDao.deleteAll()
        }
    }

    /**
     * Function to delete all the Spent in the Spent Database
     *
     */
    fun deleteAllSpent() {
        viewModelScope.launch {
            spentDao.deleteAll()
        }
    }

    /**
     * Function to update the Total Budget amount because it is un-clickable now.
     *
     * @param budgetId the budgets id to insert or update
     * @param amountToAdd the amount that is added to the initial amount
     *
     */
    fun updateBudgetAmount(budgetId: Int, amountToAdd: Long) {
        viewModelScope.launch {
            budgetDao.updateBudgetAmount(budgetId, amountToAdd)
        }
    }
}
