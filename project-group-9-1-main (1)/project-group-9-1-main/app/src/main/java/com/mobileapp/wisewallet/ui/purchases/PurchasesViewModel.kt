/*
    This file is the view model for the purchases fragment. It has
    yet to be integrated with the fragment itself
 */
package com.mobileapp.wisewallet.ui.purchases

import com.mobileapp.wisewallet.database.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.DateTimeException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoField

class PurchasesViewModel {
    /*
    // queries all transactions from the database based on filter menu inputs.
    // if no filters are used, all transactions will be queried
    // It takes an argument of the current search string so that only transactions that
    // contain the search argument are queried
    private suspend fun loadTransactions(searchText: String) {

        val minAmount = popupBinding.editTextText4.text
            .toString()
            .toDoubleOrNull()
            ?.let { (it * 100).toLong() }
        val maxAmount = popupBinding.editTextText3.text
            .toString()
            .toDoubleOrNull()
            ?.let { (it * 100).toLong() }

        val f = DateTimeFormatterBuilder().apply {
            appendPattern("M/d")
            optionalStart()
            appendPattern("/u")
            optionalEnd()
        }.toFormatter()
        val iso = DateTimeFormatter.ISO_DATE_TIME

        val minDate = try {
            val accessor = f.parse(popupBinding.editTextDate.text.toString())
            val date = LocalDate.now()
                .with(ChronoField.MONTH_OF_YEAR, accessor.getLong(ChronoField.MONTH_OF_YEAR))
                .with(ChronoField.DAY_OF_MONTH, accessor.getLong(ChronoField.DAY_OF_MONTH))
                .let {
                    if (accessor.isSupported(ChronoField.YEAR)) {
                        it.with(ChronoField.YEAR, accessor.getLong(ChronoField.YEAR))
                    } else {
                        it
                    }
                }
            LocalDateTime.of(date, LocalTime.MIN)
        } catch (e: DateTimeParseException) {
            null
        } catch (e: DateTimeException) {
            null
        }
        val maxDate = try {
            val accessor = f.parse(popupBinding.editTextDate2.text.toString())
            val date = LocalDate.now()
                .with(ChronoField.MONTH_OF_YEAR, accessor.getLong(ChronoField.MONTH_OF_YEAR))
                .with(ChronoField.DAY_OF_MONTH, accessor.getLong(ChronoField.DAY_OF_MONTH))
                .let {
                    if (accessor.isSupported(ChronoField.YEAR)) {
                        it.with(ChronoField.YEAR, accessor.getLong(ChronoField.YEAR))
                    } else {
                        it
                    }
                }
            LocalDateTime.of(date, LocalTime.MAX)
        } catch (e: DateTimeParseException) {
            null
        } catch (e: DateTimeException) {
            null
        }

        val budgetAdapter = popupBinding.categorySpinner.adapter as CustomSpinnerAdapter
        val selectedBudgets = budgetAdapter.checkedItems.asSequence()
            .mapIndexedNotNull { i, sel ->
                if (sel) {
                    budgetAdapter.items[i]
                } else null
            }
            .mapNotNull { name ->
                budgets.find { it.name == name } ?.id
            }
            .toList()

        val accountAdapter = popupBinding.accountSpinner.adapter as CustomSpinnerAdapter
        val selectedSources = accountAdapter.checkedItems.asSequence()
            .mapIndexedNotNull { i, sel ->
                if (sel) {
                    accountAdapter.items[i]
                } else null
            }
            .toList()
        val selectedAccounts = selectedSources.asSequence()
            .filter { it.startsWith("Account: ") }
            .map { it.removePrefix("Account: ") }
            .mapNotNull { n -> accounts.find { it.name == n } ?.id }
            .toList()
        val selectedCards = selectedSources.asSequence()
            .filter { it.startsWith("Card: ") }
            .map { it.removePrefix("Card: ") }
            .mapNotNull { n -> cards.find { it.name == n } ?.id }
            .toList()

        val transactions = database.transactionDao().getTransactions()

        val inCostRangeTransactions = transactions.let { list ->
            minAmount?.let { amt ->
                list.filter { it.amount >= amt }
            } ?: list
        }.let { list ->
            maxAmount?.let { amt ->
                list.filter { it.amount <= amt }
            } ?: list
        }

        val accountFilteredTransactions = inCostRangeTransactions.let { list ->
            if (selectedSources.isEmpty()) {
                list
            } else {
                list.filter { trans ->
                    if (trans.sourceType == Transaction.SOURCE_ACCOUNT) {
                        selectedAccounts.contains(trans.sourceId)
                    } else {
                        selectedCards.contains(trans.sourceId)
                    }
                }
            }
        }

        val budgetFilteredTransactions = accountFilteredTransactions.let { list ->
            if (selectedBudgets.isEmpty()) {
                list
            } else {
                list.filter { trans -> selectedBudgets.contains(trans.budgetId) }
            }
        }

        var dateFilteredTransactions = budgetFilteredTransactions.let { list ->
            minDate?.let { date ->
                val dateStr = iso.format(date)
                list.filter { it.date >= dateStr }
            } ?: list
        }.let { list ->
            maxDate?.let { date ->
                val dateStr = iso.format(date)
                list.filter { it.date <= dateStr }
            } ?: list
        }

        if (searchText.isNotEmpty()) {
            dateFilteredTransactions = dateFilteredTransactions.filterNot { it.description.contains(searchText) }
        }

        withContext(Dispatchers.Main) {
            binding.recycleView.adapter = Adapter(translateTransactions(dateFilteredTransactions))
        }
    }

    // converts database transaction objects to RecyclerView-friendly objects
    // It takes a list of transaction database objects as an argument and returns
    // a list of TransactionData objects that can be used in the RecyclerView
    private fun translateTransactions(list: List<Transaction>): List<TransactionData> {
        return list.map { trans ->
            TransactionData(
                transactionAmount = trans.amount / 100.0,
                accountBalance = 0.0,
                vendor = trans.description,
                account = if (trans.sourceType == Transaction.SOURCE_ACCOUNT) {
                    (accounts.find { it.id == trans.sourceId })?.name ?: "Unknown Account"
                } else {
                    (cards.find { it.id == trans.sourceId })?.name ?: "Unknown Card"
                }
            )
        }
    }
    */

}