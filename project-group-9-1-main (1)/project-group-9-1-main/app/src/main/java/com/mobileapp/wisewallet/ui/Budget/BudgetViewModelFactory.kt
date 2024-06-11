// BudgetView Model Factory used to create an instance of the BudgetViewModelFactory made obsolete by some code in BudgetViewModel

package com.mobileapp.wisewallet.ui.Budget

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.Factory

class BudgetViewModelFactory(private val context: Context) : Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BudgetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BudgetViewModel(context) as T
        }
        throw IllegalArgumentException("ViewModel class ${modelClass.simpleName} is not supported by this factory")
    }
}


