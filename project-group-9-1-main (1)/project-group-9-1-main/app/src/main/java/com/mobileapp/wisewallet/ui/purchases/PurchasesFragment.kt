/*
    This file implements the logic for the transaction screen of the app. It pulls all the transaction data
    from our room database and displays each transaction in a recycler view. Filtering functionality is also
    implemented so that the transaction view can be changed to show transactions within certain amounts and dates,
    certain accounts, and certain categories.
 */
package com.mobileapp.wisewallet.ui.purchases

import android.annotation.SuppressLint
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.PopupWindow
import android.widget.Spinner
import androidx.compose.ui.graphics.Color
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobileapp.wisewallet.database.Account
import com.mobileapp.wisewallet.database.Budget
import com.mobileapp.wisewallet.database.Card
import com.mobileapp.wisewallet.database.Transaction
import com.mobileapp.wisewallet.database.WWDatabase
import com.mobileapp.wisewallet.databinding.FragmentPurchasesBinding
import com.mobileapp.wisewallet.databinding.PopupLayoutBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DateTimeException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoField


class PurchasesFragment : Fragment() {

    private var _binding: FragmentPurchasesBinding? = null
    private val binding get() = _binding!!

    private lateinit var popupBinding: PopupLayoutBinding
    private lateinit var popupWindow: PopupWindow
    private lateinit var budgets: List<Budget>
    private lateinit var accounts: List<Account>
    private lateinit var cards: List<Card>

    private lateinit var database: WWDatabase
    lateinit var transData: List<TransactionData>

    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        database = WWDatabase.getInstance(this.requireContext())

        _binding = FragmentPurchasesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        popupBinding = PopupLayoutBinding.inflate(layoutInflater, null, false)
        popupWindow = PopupWindow(
            popupBinding.root,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            this.width = 1000       // set width and height of filter menu
            this.height = 1000
            this.elevation = 10f    // set elevation to make filter appear "above" fragment
            this.setBackgroundDrawable(ColorDrawable(Color.White.hashCode()))   // set white color for filter menu
            this.isOutsideTouchable = true      // close menu by touching elsewhere
        }

        val categorySpinner: Spinner = popupBinding.categorySpinner
        val accountSpinner: Spinner = popupBinding.accountSpinner


        popupBinding.button.setOnClickListener {    // when the apply button is tapped
            lifecycleScope.launch {
                loadTransactions(binding.editTextText.text.toString())  // load transactions that meet the filter requirements
            }
        }

        binding.filterButton.setOnClickListener {
            popupWindow.showAtLocation(view, Gravity.CENTER, 0, -200)   // show filter menu popup
        }

        val recyclerView = binding.recycleView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        lifecycleScope.launch {
            budgets = database.budgetDao().getBudgetsAlphabetical()
            accounts = database.accountDao().getAccounts()
            cards = database.cardDao().getCards()

            withContext(Dispatchers.Main) {
                // create spinner adapter for categories
                val categoryAdapter = CustomSpinnerAdapter(requireContext(), budgets.map { it.name })
                // create spinner adapter for accounts
                val accountAdapter = CustomSpinnerAdapter(
                    requireContext(),
                    accounts.map { "Account: ${it.name}" } + cards.map { "Card: ${it.name}" }
                )

                categorySpinner.adapter = categoryAdapter       // set spinners to use custom adapter
                accountSpinner.adapter = accountAdapter
            }

            val trans = database.transactionDao().getTransactions()
            transData = translateTransactions(trans)
            withContext(Dispatchers.Main) {
                // set recycler view to display all transactions from the database
                recyclerView.adapter = Adapter(transData)
            }

        }
        // updates the recycler view based on what it entered into the search bar.
        binding.editTextText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (binding.editTextText.text.isEmpty()) {
                    lifecycleScope.launch {
                        loadTransactions("")
                    }
                }
                val filteredList = transData.filter { it.vendor.contains(binding.editTextText.text.toString()) }
                recyclerView.adapter = Adapter(filteredList)
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        return root
    }

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
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}