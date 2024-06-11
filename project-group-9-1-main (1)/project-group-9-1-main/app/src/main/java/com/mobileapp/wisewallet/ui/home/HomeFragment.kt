/*
    This file implements the logic for the home screen of the app. It allows you to view
    all your accounts and stocks. It allows you to add amnual and plaid accounts as well as stocks
 */
package com.mobileapp.wisewallet.ui.home

import android.annotation.SuppressLint
import android.graphics.Color.parseColor
import android.os.Bundle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.mobileapp.wisewallet.database.Transaction
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.TextField
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.mobileapp.wisewallet.R
import com.mobileapp.wisewallet.database.Account
import com.mobileapp.wisewallet.database.Card
import com.mobileapp.wisewallet.database.Stock
import com.mobileapp.wisewallet.databinding.FragmentHomeBinding
import com.mobileapp.wisewallet.network.LinkTokenRequester
import com.mobileapp.wisewallet.plaid.AccessTokenResponse
import com.mobileapp.wisewallet.plaid.BalanceResponse
import com.mobileapp.wisewallet.plaid.PlaidTransaction
import com.mobileapp.wisewallet.plaid.PlaidTransactionResponse
import com.mobileapp.wisewallet.ui.CurrencyVisualTransformation
import com.plaid.link.FastOpenPlaidLink
import com.plaid.link.Plaid
import com.plaid.link.PlaidHandler
import com.plaid.link.configuration.LinkTokenConfiguration
import com.plaid.link.result.LinkExit
import com.plaid.link.result.LinkSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
/**
 * Hexadecimal color constant used for theming/styling UI elements.
 */
const val color = 0xFF81D4FA
/**
 * Fragment class for handling the home screen operations.
 * @author Dev Patel
 */
class HomeFragment : Fragment(), HasDefaultViewModelProviderFactory  {
    /**
     * Activity result launcher for
     * handling the result of launching Plaid link.
     */
    var plaidHandler: PlaidHandler? = null
    val linkAccountToPlaid = registerForActivityResult(FastOpenPlaidLink()) { result ->
        when (result) {
            is LinkSuccess -> showSuccess(result)
            is LinkExit -> showFailure(result)
        }
    }
    /**
     * Exchange a public token
     *  for an access token using the Plaid API.
     */
    private fun tokenSwap(publicToken: String) {
        val callSetAccessToken = LinkTokenRequester.api.setAccessToken(publicToken)
        callSetAccessToken.enqueue(object : Callback<AccessTokenResponse> {
            override fun onResponse(call: Call<AccessTokenResponse>, response: Response<AccessTokenResponse>) {
                if (response.isSuccessful) {
                    val accessTokenResponse = response.body()
                    val accessToken = accessTokenResponse?.accessToken

                    if (accessToken != null) {
                        // Access token obtained successfully
                        Log.d("setAccessToken", "Access token set successfully: $accessToken")
                        fetchPlaidData()
                        //fetchTransactions()
                        //fetchBalance()
                    } else {
                        Log.e("setAccessToken", "Access token is null")
                    }
                } else {
                    Log.e("setAccessToken", "Error setting access token: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<AccessTokenResponse>, t: Throwable) {
                Log.e("setAccessToken", "Error setting access token: ", t)
            }
        })
    }

    /**
     * Fetches Account and Transaction data from Plaid,
     *      and inserts it into the Database.
     */
    fun fetchPlaidData() {
        (activity?.lifecycleScope ?: this.lifecycleScope).launch {
            val balances = getApiBalances() ?: return@launch
            val accountMap = insertAndMapBalances(balances)
            val transactions = getApiTransactions() ?: return@launch
            insertTransactionsWithMap(transactions, accountMap)
        }
    }

    /**
     * Retrieves the Transaction data from the Plaid API.
     *
     * @return a list of Plaid Transactions, or null if the API did not return successfully.
     */
    private suspend fun getApiTransactions(): List<PlaidTransaction>? {
        val call: Call<PlaidTransactionResponse> = LinkTokenRequester.api.getTransactions()
        val response = suspendCancellableCoroutine { continuation ->
            call.enqueue(object : Callback<PlaidTransactionResponse> {
                override fun onResponse(
                    call: Call<PlaidTransactionResponse>,
                    response: Response<PlaidTransactionResponse>
                ) {
                    continuation.resume(response)
                }

                override fun onFailure(call: Call<PlaidTransactionResponse>, t: Throwable) {
                    continuation.resumeWithException(t)
                }
            })
        }
        return if (response.isSuccessful) {
            val transaction = response.body()
            Log.d("fetchTransactions", "Response body: $transaction")
            transaction?.addedTransactions
        } else {
            Log.d("fetchTransactions", "Unsuccessful response: ${response.errorBody()?.string()}")
            null
        }
    }


    /**
     * Inserts Plaid Transactions into the WiseWallet database.
     * Converts from Plaid account IDs to WiseWallet account IDs using the provided map.
     *
     * @param transactions a List of Plaid Transactions.
     * @param map a map from Plaid account IDs to WiseWallet account IDs.
     */
    private suspend fun insertTransactionsWithMap(transactions: List<PlaidTransaction>, map: Map<String, Long>) {
        val roomTransactions = transactions.mapNotNull { plaidTransaction ->
            map[plaidTransaction.accountId]?.let { accountId ->
                var budget = 5 // Other category
                if (plaidTransaction.category.contains("Rent")) {
                    budget = 1  // rent category
                } else if (plaidTransaction.category.contains("Food and Drink")) {
                    budget = 2  // Grub category
                } else if (plaidTransaction.category.contains("Travel")) {
                    budget = 3  // travel category
                } else if (plaidTransaction.category.contains("Entertainment")) {
                    budget = 4  // entertainment category
                }
                Transaction(
                    description = plaidTransaction.name,
                    sourceId = accountId.toInt(),
                    sourceType = Transaction.SOURCE_ACCOUNT, // Assuming account-based transactions
                    amount = (plaidTransaction.amount * 100).toLong(), // Example: converting to cents
                    date = plaidTransaction.date,
                    budgetId = budget,
                )
            }
        }
        val transactionDao = viewModel.transactionDao
        transactionDao.insert(*roomTransactions.toTypedArray())
    }

    /**
     * Retrieves Balances from the Plaid API.
     *
     * @return a list of Plaid Accounts, or `null` if the Plaid API did not return successfully.
     */
    private suspend fun getApiBalances(): List<com.mobileapp.wisewallet.plaid.Account>? {
        val call: Call<BalanceResponse> = LinkTokenRequester.api.getBalance()
        val response = suspendCancellableCoroutine { continuation ->
            call.enqueue(object: Callback<BalanceResponse> {
                override fun onResponse(
                    call: Call<BalanceResponse>,
                    response: Response<BalanceResponse>
                ) {
                    continuation.resume(response)
                }

                override fun onFailure(call: Call<BalanceResponse>, t: Throwable) {
                    continuation.resumeWithException(t)
                }

            })
        }
        return if (response.isSuccessful) {
            val balanceResponse = response.body()
            Log.d("fetchBalance", balanceResponse.toString())
            // TODO: put account with balance into database
            balanceResponse?.accounts
        } else {
            null
        }
    }

    /**
     * Inserts Plaid Accounts into the Database, and generates a map
     *      from Plaid account IDs to WiseWallet account IDs.
     *
     * @param accounts a List of Plaid Accounts.
     * @return a map from Plaid account IDs to WiseWallet account IDs.
     */
    private suspend fun insertAndMapBalances(accounts: List<com.mobileapp.wisewallet.plaid.Account>): Map<String, Long> {
        return accounts.associateBy(
            keySelector = { it.accountId },
            valueTransform = {
                val account = Account(
                    name = it.name,
                    type = it.subtype,
                    balance = it.balances.available.toLong(),
                    backed = true,
                )
                viewModel.insertAndGetAccount(account)
            }
        )
    }

    fun setOptionalEventListener() = Plaid.setLinkEventListener { event ->
        Log.i("Event", event.toString())
    }
    private fun showSuccess(success: LinkSuccess) {
        // bank account information is in the metadata of the LinkSuccess object
        Log.d("showSuccess", "showSuccess called: ${success.metadata}")
        tokenSwap(success.publicToken)
    }
    private fun showFailure(exit: LinkExit) {
        Log.d("showFailure", "showFailure called")
    }
    fun onLinkTokenError(error: Throwable) {
        Log.e("onLinkTokenError", error.message.toString())
    }
    fun onLinkTokenSuccess(linkToken: String) {
        Log.d("onLinkTokenSuccess", "method called. Link token is: $linkToken")
        val tokenConfiguration = LinkTokenConfiguration.Builder()
            .token(linkToken)
            .build()
        plaidHandler = Plaid.create(requireActivity().application, tokenConfiguration)
        plaidHandler?.let { linkAccountToPlaid.launch(it) }
    }

    val viewModel: HomeViewModel by viewModels(factoryProducer = { HomeViewModel.FACTORY })

    private var _binding: FragmentHomeBinding? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                WiseWalletApp(this@HomeFragment, viewModel)
            }
        }
    }


}


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WiseWalletApp(homeFragment: HomeFragment, viewModel: HomeViewModel) {
    /**
     * Observes and stores account data
     * and stock data
     * from a Flow exposed by the ViewModel.
     */
    val accounts by viewModel.accountsFlow
        .asLiveData()
        .observeAsState(
            initial = listOf(
                Account(
                    name = "",
                    type = "",
                    balance = 0L,
                    backed = false
                )
            )
        )


    val stocks by viewModel.stocksFlow
        .asLiveData()
        .observeAsState(listOf())


    /**
     * State for handling stock symbol input in a dialog.
     * State for managing the visibility of various dialogues.
     * Handlers for various user actions like adding accounts or transactions.
     * Handler for dismissing the account dialog.
     * Remember states for input fields in the New Account Dialog.
     * Remember states for input fields in the New Transaction Dialog.
     */

    var stockSymbol = remember { mutableStateOf("") }

    val (showMenu, setShowMenu) = remember { mutableStateOf(false) }
    var showAccountDialog = remember { mutableStateOf(false) }
    var showTransactionDialog = remember { mutableStateOf(false) }
    var showStockDialog = remember { mutableStateOf(false) } // New dialog for adding stock


    // Update this part to handle showing account or card dialog
    val onAddAccountClicked = { showAccountDialog.value = true }
    val onAddTransaction = {showTransactionDialog.value = true}
    val onAddStockClicked = { showStockDialog.value = true } // Manage stock dialog



    val onAccountDismiss = { showAccountDialog.value = false }
    var newBankName = remember { mutableStateOf("") }
    var newAccountType = remember { mutableStateOf("") }
    var newBalance = remember { mutableStateOf("") }

    /**
     *  Confirm handler for creating a new account from dialog input.
     *  Displays the New Account Dialog if required.
     *  Displays the Transaction Input Dialog for the current account.
     *  Displays the Stock Dialog if required.
     */

    val onConfirm: (String, String, String) -> Unit = { bankName, accountType, balance ->
        val newB = balance.toLong()
        val newAccount = Account(bankName, accountType, newB, false)
        viewModel.insertAccount(newAccount)
        showAccountDialog.value = false
        // Reset fields
        newBankName.value = ""
        newAccountType.value = ""
        newBalance.value = ""
    }


    // When showDialog.value is true, the dialog will be displayed
    if (showAccountDialog.value) {
        NewAccountDialog(
            onDismiss = onAccountDismiss,
            onConfirm = onConfirm,
            newBankName = newBankName,
            newAccountType = newAccountType,
            newBalance = newBalance
        )
    }

    var currentAccount by remember { mutableStateOf<Account?>(null) }

    // Now use currentAccount to pass to TransactionInputDialog if it's not null
    if (showTransactionDialog.value && currentAccount != null) {
        TransactionInputDialog(
            account = currentAccount!!,  // Use the current account
            onDismiss = {
                showTransactionDialog.value = false
                currentAccount = null  // Reset current account when dismissing
            },
            onAddTransaction = { transaction ->
                viewModel.insertTransaction(transaction)
                showTransactionDialog.value = false  // Close the dialog upon successful transaction addition
                currentAccount = null  // Reset current account when transaction added
            }
        )
    }



    // When showDialog.value is true, the dialog will be displayed
    if (showAccountDialog.value) {
        NewAccountDialog(
            onDismiss = onAccountDismiss,
            onConfirm = onConfirm,
            newBankName = newBankName,
            newAccountType = newAccountType,
            newBalance = newBalance
        )
    }
    if (showStockDialog.value) {
        AlertDialog(
            onDismissRequest = { showStockDialog.value = false },
            title = { Text("Add Stock") },
            text = {
                Column {
                    TextField(
                        value = stockSymbol.value,
                        onValueChange = { stockSymbol.value = it },
                        label = { Text("Stock Symbol") },
                        placeholder = { Text("AAPL:NASDAQ") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.fetchStockData(stockSymbol.value)
                        showStockDialog.value = false
                        stockSymbol.value = ""  // Reset after fetching
                    }
                ) { Text("Add") }
            },
            dismissButton = {
                Button(onClick = { showStockDialog.value = false }) { Text("Cancel") }
            }
        )
    }


    /**
     *  Main UI Scaffold
     */
    Scaffold(
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            // Use a Box to overlay the button and the menu
            Box {
                FloatingActionButton(onClick = { setShowMenu(true) }, containerColor = Color(color)) {
                    Icon(Icons.Filled.Add, contentDescription = "Add")
                }
                // DropdownMenu for selecting between card and account
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { setShowMenu(false) }
                ) {
                    DropdownMenuItem(onClick = {
                        onAddAccountClicked()

                        setShowMenu(false)
                    }) {
                        Text("Add Account")
                    }
                    DropdownMenuItem(onClick = {
                        onAddStockClicked()
                        showStockDialog.value = true
                        setShowMenu(false)
                    }) {
                        Text("Add Stock")
                    }
                    DropdownMenuItem(onClick = {
                        Log.i("plaidTag", "plaid button has been clicked")
                        homeFragment.setOptionalEventListener()
                        LinkTokenRequester.token.subscribe(homeFragment::onLinkTokenSuccess, homeFragment::onLinkTokenError)
                        setShowMenu(false)
                    }) {
                        Text("via Plaid")
                    }
                }
            }
        }
    ) { innerPadding ->
        Surface(modifier = Modifier.fillMaxSize()) {
            if (accounts.isEmpty() && stocks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("To start adding accounts or stocks, click the add button.", modifier = Modifier.padding(16.dp))
                }
            } else {
                LazyColumn(modifier = Modifier.padding(innerPadding)) {
                    item {
                        // You might want to keep the padding consistent with the rest of the items
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    item {
                        ElevatedCard(modifier = Modifier.fillMaxWidth().padding(8.dp).wrapContentWidth(Alignment.Start), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                            Text("My Wallet:", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(16.dp))
                        }
                    }
                    if (accounts.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                Text("To start adding accounts, click the add button.", modifier = Modifier.padding(16.dp))
                            }
                        }
                    } else {
                        items(accounts) { accounts ->
                            AccountCardItem(
                                accounts = accounts,
                                onDelete = { accountToDelete ->
                                    viewModel.deleteAccount(accountToDelete)
                                },
                                onAddTransaction = { accountToAddTransaction ->
                                    if (!accountToAddTransaction.backed)
                                        onAddTransaction()
                                },
                                setCurrentAccount = { acc ->
                                    currentAccount = acc
                                }  // This sets the current account

                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    item {
                        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    }
                    item {
                        ElevatedCard(modifier = Modifier.fillMaxWidth().padding(8.dp).wrapContentWidth(Alignment.Start), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                            Text("My Symbols:", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(16.dp))
                        }
                    }
                    if (stocks.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                Text("To start adding stocks, click the add button.", modifier = Modifier.padding(16.dp))
                            }
                        }
                    } else {
                        items(stocks) { stock ->
                            StockDataView(stock = stock, onDelete = { selectedStock ->
                                viewModel.deleteStock(selectedStock)
                            })
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Displays the account cards to UI
 *
 * @param accounts a List of all accounts in DB.
 * @param onDelete delete account from DB and UI
 * @param setCurrentAccount match account to added transactions
 */
@Composable
fun AccountCardItem(accounts: Account, onDelete: (Account) -> Unit, onAddTransaction: (Account) -> Unit, setCurrentAccount: (Account) -> Unit) {
    val typography = MaterialTheme.typography
    var showMenu by remember { mutableStateOf(false) }

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bank Icon - You will need to add these icons to your drawable resource
            Image(
                painter = painterResource(id = getBankIconResource(accounts.name)),
                contentDescription = "${accounts.name} logo",
                modifier = Modifier.size(40.dp) // Adjust size as needed
            )


            IconButton(onClick = { showMenu = !showMenu }) {
                Icon(Icons.Filled.MoreVert, contentDescription = "Menu")
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                if (!accounts.backed){
                DropdownMenuItem(onClick = {
                    setCurrentAccount(accounts)
                    onAddTransaction(accounts)
                    showMenu = false
                    //onAddTransaction(accounts)

                }) {
                    Text("Add Transaction")
                }}
                DropdownMenuItem(onClick = {
                    onDelete(accounts)
                    showMenu = false
                }) {
                    Text("Delete")
                }
            }

            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = accounts.name, style = typography.titleLarge)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = accounts.type, style = typography.bodyMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Balance: \$${CurrencyVisualTransformation.transformText(accounts.balance.toString())}",
                    style = MaterialTheme.typography.bodySmall)

            }
        }
    }
}
// Helper function to get the resource ID of the bank icon based on the bank name
@Composable
fun getBankIconResource(bankName: String): Int {
    return when (bankName) {
        "TD" -> R.drawable.ic_td_bank // Replace with actual drawable resource names
        "DCU" -> R.drawable.ic_dcu_bank
        // Add other banks here
        else -> R.drawable.ic_default_bank // A default bank icon
    }
}

/**
 * Displays dialog to add a manual account
 *
 * @param onDismiss to make dialog disappear
 * @param onConfirm add new account to DB
 * @param newBankName new bank name
 * @param newBalance new bank balance
 * @param newAccountType new bank type
 * @return new account info
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewAccountDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit,
    newBankName: MutableState<String>,
    newAccountType: MutableState<String>,
    newBalance: MutableState<String>
) {
    var bankNameError by remember { mutableStateOf(false) }
    var accountTypeError by remember { mutableStateOf(false) }
    var balanceError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Account") },
        text = {
            Column {
                TextField(
                    value = newBankName.value,
                    onValueChange = {
                        newBankName.value = it
                        bankNameError = it.isBlank() // Set error if blank
                    },
                    label = { Text("Bank Name") },
                    isError = bankNameError,
                    singleLine = true
                )
                TextField(
                    value = newAccountType.value,
                    onValueChange = {
                        newAccountType.value = it
                        accountTypeError = it.isBlank() // Set error if blank
                    },
                    label = { Text("Account Type") },
                    isError = accountTypeError,
                    singleLine = true
                )
                TextField(
                    value = newBalance.value,
                    onValueChange = {
                        if (it.matches(Regex("^\\d*(\\.\\d{1,2})?$"))) {
                            newBalance.value = it
                            balanceError = false
                        } else {
                            balanceError =
                                it.isNotEmpty() // Set error if not a number and not empty
                        }
                    },
                    label = { Text("Balance") },
                    isError = balanceError,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)

                )
                if (bankNameError) {
                    Text("Bank name cannot be empty", color = MaterialTheme.colorScheme.error)
                }
                if (accountTypeError) {
                    Text(
                        "Account type cannot be empty",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                if (balanceError) {
                    Text("Balance must be a number", color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Perform validation
                    bankNameError = newBankName.value.isBlank()
                    accountTypeError = newAccountType.value.isBlank()
                    balanceError = !newBalance.value.matches(Regex("^\\d*(\\.\\d{1,2})?$"))

                    // Only call onConfirm if there are no errors
                    if (!bankNameError && !accountTypeError && !balanceError) {
                        onConfirm(newBankName.value, newAccountType.value, newBalance.value)
                        //call viewmodel
                    }

                }
            ) { Text("Add") }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/**
 * Displays new transaction dialog for manual accounts
 *
 * @param accounts account for which transaction is being added
 * @param onDismiss dismiss transaction dialog
 * @param onAddTransaction confirm and add transaction
 * @return Transaction object
 */
@Composable
fun TransactionInputDialog(
    account: Account,  // Pass the account to which the transaction is to be added
    onDismiss: () -> Unit,
    onAddTransaction: (Transaction) -> Unit  // Simplify the callback
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var budgetId by remember { mutableStateOf(1) }  // Default to 1

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Transaction") },
        text = {
            Column {
                TextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") }
                )
                TextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                TextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (YYYY-MM-DD)") }
                )
                Button(onClick = { expanded = true }) {
                    Text("Select Budget Category")
                }
                DropdownMenu(
                    expanded = expanded,  // You might want to manage this state to toggle visibility
                    onDismissRequest = { expanded = false }
                ) {
                    listOf("Rent", "Grub", "Car", "Entertainment", "Other").forEachIndexed { index, label ->
                        DropdownMenuItem(
                            onClick = { budgetId = index + 1
                                expanded = false
                            }
                        ) {
                            Text(text = label)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (description.isNotBlank() && amount.isNotBlank() && date.isNotBlank()) {
                        val newTransaction = Transaction(
                            description = description,
                            sourceId = account.id,  // Assuming `id` is a field of `Account`
                            sourceType = 1,  // Always 1 for account-based transactions
                            amount = amount.toLong(),  // Convert to cents
                            date = date,
                            budgetId = budgetId
                        )
                        onAddTransaction(newTransaction)
                    } else {
                        Toast.makeText(context, "Please fill in all fields correctly.", Toast.LENGTH_SHORT).show()
                    }
                }
            ) { Text("Add") }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
/**
 * Displays stock cards to DB
 *
 * @param stock provides current stocks in DB to display
 * @param onDelete delete stock from UI and DB
 * @return Stock object to display.
 */
@Composable
fun StockDataView(stock: Stock, onDelete: (Stock) -> Unit) {
    val backgroundColor = if (stock.change >= 0) Color(0xFF4CAF50) else Color(0xFFF44336) // Green for positive, red for negative

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp).background(backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.outlinedCardColors(backgroundColor)  // Set transparency here

    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp))  {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("${stock.symbol}", style = MaterialTheme.typography.titleLarge)
                    Text("${stock.name}", style = MaterialTheme.typography.titleMedium)
                    Text("$${stock.price}", style = MaterialTheme.typography.titleMedium)
                }
                IconButton(onClick = { onDelete(stock) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Stock")
                }
            }
            Spacer(Modifier.weight(1f)) // This spacer pushes everything else down
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Box(
                    modifier = Modifier
                        .background(Color.White, shape = RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        "${stock.change}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}


