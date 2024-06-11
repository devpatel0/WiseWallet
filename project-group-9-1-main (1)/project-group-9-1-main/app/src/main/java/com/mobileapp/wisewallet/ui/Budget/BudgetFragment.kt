// This file is used to first of all create all the UI and frontend technology of the Budget Screen.
// It is essentially created to hold 6 progress bars that update simultaniously with the spent data
// that is updated from the transaction data. That is then used to calculate the progress of the progress bar
// depending on the budget values entered by the user. THe frontend is then linked to the backend RoomDB.
// In this file specifically the backend information is stored in Budget and Spent Databases and the transacation
// data is also used to update the spent Database and bring the app together.
package com.mobileapp.wisewallet.ui.Budget

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import com.example.jetpack.ui.theme.JetpackComposeTheme
import java.time.LocalDate
import androidx.lifecycle.asLiveData
import com.mobileapp.wisewallet.database.Budget
import com.mobileapp.wisewallet.database.Spent
import com.mobileapp.wisewallet.database.Transaction
import com.mobileapp.wisewallet.databinding.FragmentHomeBinding
import com.mobileapp.wisewallet.ui.home.WiseWalletApp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState



/**
 * Class that defines the BudgetFragment
 */
class BudgetFragment: Fragment(), HasDefaultViewModelProviderFactory {
    companion object {
        fun newInstance() = BudgetFragment()
    }

    // Initializes the budgets view model

    private val budgetViewModel: BudgetViewModel by viewModels(factoryProducer = { BudgetViewModel.FACTORY })

    private var _binding: FragmentHomeBinding? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                JetpackComposeTheme {
                    BudgetContent(budgetViewModel)
                }
            }
        }
    }
}

/**
 * Function that displays all the UI for budgetFragment and implements the backend as well
 *
 * @param viewModel Passes the viewModel that holds all the BudgetFragments data
 */
@Composable
fun BudgetContent(viewModel: BudgetViewModel) {

    //Initializes all the databases needed as live data

    val budgets by viewModel.budgets
        .asLiveData()
        .observeAsState(initial = listOf(Budget(0L)))

    val spent by viewModel.spent
        .asLiveData()
        .observeAsState(initial = listOf(Spent(0L)))

    val transaction = viewModel.transaction
        .asLiveData()
        .observeAsState()

    // Initializes spent that is used to  store calculated spent values

    var spentL = remember { mutableListOf(
        0L, 0L, 0L, 0L, 0L, 0L
    )
    }

    // Injects data into spent viewModel if the database is empty

    LaunchedEffect(spent) {
        if (spent.isEmpty()) {
            viewModel.deleteAllSpent()
            viewModel.insertSpent(Spent(id = 1, name = "Total Budget", spentAmount = 0L))
            viewModel.insertSpent(Spent(id = 2, name = "Rent", spentAmount = 0L))
            viewModel.insertSpent(Spent(id = 3, name = "Grub", spentAmount = 0L))
            viewModel.insertSpent(Spent(id = 4, name = "Car", spentAmount = 0L))
            viewModel.insertSpent(Spent(id = 5, name = "Entertainment", spentAmount = 0L))
            viewModel.insertSpent(Spent(id = 6, name = "Other", spentAmount = 0L))
        }
    }

    LaunchedEffect(spent) {

        // Initializes a list with all the transaction data

        val latestTransactionList: List<Transaction> = transaction.value?.toList() ?: emptyList()
        Log.d("BHELLOE", "BuHELLOOOOO")

        // Calls calculate spent function

        calculateSpent(latestTransactionList, spentL)
        Log.d("BHELLOE", "BuHELLOOOOO")

        // Goes through the updated spent data and updates the spent database

        spentL.forEachIndexed { index, amount ->
            var label = ""
            if (index == 0) {
                label = "Total Budget"
            } else if (index == 1) {
                label = "Rent"
            } else if (index == 2) {
                label = "Grub"
            } else if (index == 3) {
                label = "Car"
            } else if (index == 4) {
                label = "Entertainment"
            } else if (index == 5) {
                label = "Other"
            }
            val spend = Spent(index + 1, label, amount)
            viewModel.updateSpent(spend)
        }
    }

    // Injects data into budgets viewModel if the database is empty

    LaunchedEffect(budgets) {
        if (budgets.isEmpty()) {
            viewModel.deleteAllBudgets()
            viewModel.insertBudget(Budget(id = 1, name = "Total Budget", budgetAmount = 0L))
            viewModel.insertBudget(Budget(id = 2, name = "Rent", budgetAmount = 0L))
            viewModel.insertBudget(Budget(id = 3, name = "Grub", budgetAmount = 0L))
            viewModel.insertBudget(Budget(id = 4, name = "Car", budgetAmount = 0L))
            viewModel.insertBudget(Budget(id = 5, name = "Entertainment", budgetAmount = 0L))
            viewModel.insertBudget(Budget(id = 6, name = "Other", budgetAmount = 0L))
        }
    }

    val labels = remember { mutableListOf(
        "Total Budget", "Rent", "Grub", "Car", "Entertainment", "Other"
    )
    }

    val prog = remember { mutableStateOf(mutableListOf<Double>()) }

    /**
     * Function to update the progress bar based on spent and budget data
     *
     * @param budgets List of Budget objects defined above
     * @param spent List of Spent objects defined above
     */
    fun updateProgress(budgets: List<Budget>, spent: List<Spent>) {
        // Goes through finds all digits in spent and budget and pushes to progress list
        val newP = mutableListOf<Double>()
        if (budgets.isEmpty() || spent.isEmpty()) {
            newP.add(0.0)
            newP.add(0.0)
            newP.add(0.0)
            newP.add(0.0)
            newP.add(0.0)
            newP.add(0.0)
        } else {
            // Loops through 6 times to update all progress bars
            for (i in 0 until 6) {
                val b = budgets.getOrNull(i)?.budgetAmount ?: 0.0
                val s = spent.getOrNull(i)?.spentAmount ?: 0.0

                val doubleS = s.toDouble()
                val doubleB = b.toDouble()

                // Simple division in terms of finding the percent of progress

                val divide: Double = if (doubleB != 0.0) {
                    doubleS / doubleB
                } else {
                    0.0
                }
                newP.add(divide)
            }
        }
        prog.value = newP
    }

    updateProgress(budgets, spent)

    // This function is called everytime budgets or spent is mutated

    LaunchedEffect(budgets, spent) {
        val budgetsList = budgets
        updateProgress(budgetsList, spent)
    }

    //Gets the current month and year

    val currentMonth = LocalDate.now().monthValue
    val currentYear = LocalDate.now().year

    val months = listOf(
        "January", "February", "March",
        "April", "May", "June",
        "July", "August", "September",
        "October", "November", "December"
    )

    // UI elements utilized by jetpack compose start here
    // Column to hold and place the text value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Prints the month and year in this column area

        Text(
            text = "${months[currentMonth - 1]} $currentYear",
            style = MaterialTheme.typography.h5.copy(fontSize = 45.sp),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 5.dp)
        )

        // Iterates through and edits each progress bars progress and updates other strings

        val minSize = minOf(budgets.size, spent.size, labels.size)
        for (index in 1 until minSize+1) {
            var value = prog.value.getOrNull(index-1) ?: 0.0
            StackedProgressBar(

                // Receives the current data to use in the function

                i = index,
                progress = value.toFloat(),
                spent = "Spent: $${String.format("%.2f", spent[index-1].spentAmount.toDouble())}",
                budget = "Budget: $${String.format("%.2f", budgets[index-1].budgetAmount.toDouble())}",
                text = labels[index-1],

                // On the change of a budget value these changes are made

                onBudgetChange = { index, newBudgetValue ->
                    val amount = newBudgetValue.budgetAmount
                    val newB = Budget(
                        index,
                        labels[index-1],
                        amount
                    )

                    // Updates each budget when updated by the user

                    viewModel.updateBudget(newB)

                    // Updates the total budget each time the user updates a budget

                    viewModel.updateBudgetAmount(1, (amount-budgets[index-1].budgetAmount))
                }
            )
        }
    }
}

/**
 * Defining the StackProgressBar function and the UI aswell
 *
 * @param i The index of the current progress bar being worked onm
 * @param progress A float value of the current progress
 * @param spent A string value of the current progress bars spent data along with the pre-string in front of the spent value
 * @param budget A string value of the current progress bars budget data along with the pre-string in front of the budget value
 * @param text The current progress bars category as a text
 * @param onBudgetChange Function that updates the budget amount in the budget database depending on input
 */
@Composable
fun StackedProgressBar(
    i: Int,
    progress: Float,
    spent: String,
    budget: String,
    text: String,
    onBudgetChange: (Int, Budget) -> Unit
) {

    // Declaring needed variables like the color that is mutatable and the spent and budget values

    var color = Color.Green
    val spentNumber = spent.substring(8).toDouble()
    val budgetNumber = budget.substring(9).toDouble()
    if(spentNumber > budgetNumber)
    {
        color = Color.Red
    }

    // Defines popup alert to enter budget which only appears when progress bar is clicked

    val showDialog = remember { mutableStateOf(false) }

    val budgetTextFieldValue = remember { mutableStateOf(TextFieldValue()) }

    val (progressBarWidth, setProgressBarWidth) = remember { mutableStateOf(0) }

    // Creates the initial box to hold the progress bar parts
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .padding(4.dp)
            .background(Color.Transparent)
            .border(2.dp, Color.Black, RoundedCornerShape(8.dp))

            // Makes it so total budget is not clickable

            .let {
                if (text != "Total Budget") it.clickable { showDialog.value = true } else it
            }
            .onGloballyPositioned { layoutCoordinates ->
                val width = layoutCoordinates.size.width
                setProgressBarWidth(width)
            },
        contentAlignment = Alignment.CenterStart
    ) {

        // Makes the non progress part white and fixes up the UI

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(1f - progress)
                .background(Color.White, RoundedCornerShape(8.dp))
        )

        // Makes the progress part green and fixes up the UI

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress)
                .background(color, RoundedCornerShape(8.dp))
        )

        //Adds The certain category of budget string into the progress bar

        Text(
            text = text,
            style = MaterialTheme.typography.body1.copy(fontSize = 30.sp),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp)
        )

        //Adds budget which is initially blank into the progress bar and gets updated later in the code above using the database

        Box(
            modifier = Modifier
                .padding(end = 8.dp, bottom = 25.dp)
                .fillMaxWidth()
                .widthIn(min = (progressBarWidth.dp - 5.dp), max = progressBarWidth.dp)
        ) {
            Text(
                text = budget,
                style = MaterialTheme.typography.body1.copy(fontSize = 18.sp),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }

        //Adds spent which is initially blank into the progress bar and gets updated later in the code above

        Box(
            modifier = Modifier
                .padding(end = 8.dp, top = 25.dp)
                .fillMaxWidth()
                .widthIn(min = (progressBarWidth.dp - 5.dp), max = progressBarWidth.dp)
        ) {
            Text(
                text = spent,
                style = MaterialTheme.typography.body1.copy(fontSize = 18.sp),
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }

        // What happens when a progress bar is clicked

        if (showDialog.value) {
            AlertDialog(
                onDismissRequest = { showDialog.value = false },
                confirmButton = {

                    // Aligns the confirm button at the bottom of the alert dialog and also aligns the textbox above it
                    // Initial Box object to hold both

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    )
                    {

                        // Initializes column to store the textbox

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Top
                        ) {
                            TextField(
                                value = budgetTextFieldValue.value,
                                onValueChange = { newValue ->

                                    // Updates a the budget string when the value is changed and the button is clicked
                                    //Can only input digits of a certain length

                                    val currentValue = budgetTextFieldValue.value.text
                                    val newValueText = newValue.text
                                    if (newValueText.length <= 6 && newValueText.all { it.isDigit() }) {
                                        budgetTextFieldValue.value = newValue
                                    } else if (newValueText.length < currentValue.length && currentValue.length == 6) {
                                        val filteredValue = newValueText.filter { it.isDigit() }
                                        budgetTextFieldValue.value = TextFieldValue(text = filteredValue)
                                    }
                                },
                                singleLine = true, // Can not enter to more then one line
                                placeholder = { Text("Enter budget amount") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.padding(16.dp)
                            )
                            Button(
                                onClick = {

                                    // When the button is clicked on change is called with these values and the alert dialog goes away
                                    val textF = budgetTextFieldValue.value.text
                                    if(textF.isNotBlank()) {
                                        val updatedBudget = Budget(
                                            i,
                                            text,
                                            budgetAmount = textF.toLong()
                                        )
                                        onBudgetChange(i, updatedBudget)
                                        showDialog.value = false
                                    }},
                            )
                            {
                                Text("OK")
                            }
                        }
                    }
                }
            )

        }
    }
}

/**
 * Function to calculate the Spent values from the transaction data
 *
 * @param transactions A list of Transactions
 * @param spentL a MutableList of spent data
 */
fun calculateSpent(
    transactions: List<Transaction>,
    spentL: MutableList<Long>
) {

    // Initializes each spent category

    var totalSpent = 0L
    var rent = 0L
    var grub = 0L
    var car = 0L
    var entertainment = 0L
    var other = 0L
    Log.d("BudgetUpdate", "Budget updated successfully: $transactions")

    // Goes through all the transactions and adds to the spent value that is updated

    transactions.forEach { transaction ->
        var amount = transaction.amount
        if(amount < 0)
        {
            amount = 0L
        }

        // Always adds to the total spent

        val index = transaction.budgetId
        totalSpent += amount
        if(index == 2)
        {
            rent += amount
        }
        else if(index == 3)
        {
            grub += amount
            Log.d("2222222222222222222", "456765432345676543: $transaction")
        }
        else if(index == 4)
        {
            car += amount
        }
        else if(index == 5)
        {
            entertainment += amount
        }
        else if(index == 6) {
            other += amount
        }
    }

    // Clears the spentL MutableList and adds this new data to it to update the spent database
    spentL.clear()
    spentL.addAll(listOf(totalSpent, rent, grub, car, entertainment, other))
}


