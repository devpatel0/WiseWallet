package com.mobileapp.wisewallet.ui.transfer

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import com.mobileapp.wisewallet.database.Account
import com.mobileapp.wisewallet.database.Budget
import com.mobileapp.wisewallet.database.Transaction
import com.mobileapp.wisewallet.databinding.FragmentTransferBinding
import com.mobileapp.wisewallet.ui.AppSurface
import com.mobileapp.wisewallet.ui.CurrencyVisualTransformation
import com.mobileapp.wisewallet.ui.currencyVisualTransformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * The `Fragment` for creating `Transaction`s in WiseWallet.
 */
class TransferFragment : Fragment(), HasDefaultViewModelProviderFactory {

    companion object {
        /**
         * Creates a new instance of the `TransferFragment`.
         */
        fun newInstance() = TransferFragment()
    }

    private val viewModel: TransferViewModel by viewModels(factoryProducer = { TransferViewModel.FACTORY })

    private var _binding: FragmentTransferBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                TransactionUI()
            }
        }
    }

    /**
     * Composes the main UI for the Transfer Fragment.
     */
    @Preview(
        device = Devices.PHONE,
        uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL,
        name = "Light Mode",
        showSystemUi = true
    )
    @Preview(
        device = Devices.PHONE,
        uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
        name = "Dark Mode",
        showSystemUi = true
    )
    @Composable
    fun TransactionUI() {
        val accounts by viewModel.accountsFlow
            .asLiveData().map {
                val l = mutableListOf(Account(-1, "None", "Other", 0, false))
                l.addAll(it)
                l
            }
            .observeAsState(listOf(Account(0, "Select Account", "Other", 0, false)))

        val budgets by viewModel.budgetsFlow
            .asLiveData().map {
                val l = mutableListOf(Budget(-1, "None", 0L))
                l.addAll(it)
                l
            }
            .observeAsState(listOf(Budget(-1, "None", 0L)))

        var amount by remember { mutableLongStateOf(0) }
        var fromAccount by remember { mutableStateOf(accounts[0]) }
        var toAccount by remember { mutableStateOf(accounts[0]) }
        var dialogState by remember { mutableStateOf(DialogState.NONE) }
        val transactionValid by remember { derivedStateOf {
            (fromAccount != toAccount)
                    && (fromAccount.id < 0 || amount <= fromAccount.balance)
                    && (amount != 0L)
                    && (!fromAccount.backed)
                    && (!toAccount.backed)
                    && (dialogState == DialogState.NONE)
        } }

        var budget by remember { mutableStateOf(budgets[0]) }

        val scope = rememberCoroutineScope()

        var description by remember { mutableStateOf("") }

        AppSurface {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .fillMaxHeight(0.9f),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.displaySmall,
                        text = "Transfer Money"
                    )
                    CurrencyAmount {
                        amount = it.toLongOrNull() ?: 0
                    }
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it},
                        singleLine = true,
                        placeholder = { Text(text = "Manual Transfer") },
                        label = { Text(text = "Description") }
                    )
                    BudgetSpinner(
                        budgets = budgets,
                        label = { Text(text = "Budget") },
                    ) {
                        budget = it
                    }
                    AccountSpinner(
                        accounts,
                        label = { Text(
                            color = MaterialTheme.colorScheme.onBackground,
                            text = "From"
                        ) }
                    ) {
                        fromAccount = it
                    }
                    AccountSpinner(
                        accounts,
                        label = { Text(
                            color = MaterialTheme.colorScheme.onBackground,
                            text = "To"
                        ) }
                    ) {
                        toAccount = it
                    }
                    TransactionButton(transactionValid) {
                        dialogState = DialogState.CONFIRM
                    }
                }
            }

            when (dialogState) {
                DialogState.NONE -> {}
                DialogState.CONFIRM -> ConfirmDialog(
                    onDismissRequest = { dialogState = DialogState.NONE },
                    onConfirm = {
                        dialogState = DialogState.PENDING
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                val desc = description.ifEmpty { "Manual Transfer" }
                                val date = DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.now())

                                val budgetId = if (budget.id < 0) null else budget.id

                                if (fromAccount.id >= 0) {
                                    val newFrom = fromAccount.copy(
                                        balance = fromAccount.balance - amount
                                    )
                                    viewModel.database.accountDao().update(newFrom)
                                    viewModel.database.transactionDao().insert(Transaction(
                                        0,
                                        desc,
                                        fromAccount.id,
                                        Transaction.SOURCE_ACCOUNT,
                                        amount,
                                        date,
                                        budgetId,
                                    ))
                                }
                                if (toAccount.id >= 0) {
                                    val newTo = toAccount.copy(
                                        balance = toAccount.balance + amount
                                    )
                                    viewModel.database.accountDao().update(newTo)
                                    viewModel.database.transactionDao().insert(Transaction(
                                        0,
                                        desc,
                                        toAccount.id,
                                        Transaction.SOURCE_ACCOUNT,
                                        -amount,
                                        date,
                                        budgetId,
                                    ))
                                }
                            }
                            dialogState = DialogState.SUCCESS
                        }
                    },
                    amount = amount,
                    toAccount = toAccount.name
                )
                DialogState.PENDING -> PendingDialog()
                DialogState.SUCCESS -> SuccessDialog {
                    dialogState = DialogState.NONE
                }
            }
        }
    }

    /**
     * Composes a Spinner for Accounts.
     *
     * @param accounts a List of Accounts to display in the Spinner.
     * @param label a composable label for the Spinner
     * @param onSelected a callback called when an Account is selected.
     */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun AccountSpinner(
        accounts: List<Account>,
        label: @Composable (() -> Unit)? = null,
        onSelected: (Account) -> Unit
    ) {
        var expanded by remember { mutableStateOf(false) }
        var selectedId by remember { mutableIntStateOf(accounts[0].id) }
        @SuppressLint("UnrememberedMutableState")
        val selected by derivedStateOf {
            accounts.find { it.id == selectedId }
                ?: Account(0, "Select Account", "Other", 0, false)
        }

        Column {
            label?.let { it() }
            Card(
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                ExposedDropdownMenuBox(
                    modifier = Modifier,
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                ) {
                    ListItem(
                        modifier = Modifier.menuAnchor(),
                        headlineContent = { Text(selected.name) },
                        supportingContent = {
                            Text("\$${CurrencyVisualTransformation.transformText(selected.balance.toString())}")
                        },
                        leadingContent = {
                            Icon(
                                Icons.Filled.Info,
                                null
                            )
                        }
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        for (account in accounts) {
                            DropdownMenuItem(
                                text = { Text(text = account.name) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Info,
                                        null
                                    )
                                },
                                trailingIcon = { Text("\$${CurrencyVisualTransformation.transformText(account.balance.toString())}") },
                                onClick = {
                                    selectedId = account.id
                                    expanded = false
                                    onSelected(account)
                                },
                                enabled = !account.backed,
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }

                }
            }

        }
    }

    /**
     * Composes a Spinner for Budgets.
     *
     * @param budgets a List of Budgets to display in the Spinner.
     * @param label a composable label for the Spinner
     * @param onSelected a callback called when a Budget is selected.
     */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun BudgetSpinner(
        budgets: List<Budget>,
        label: @Composable (() -> Unit)? = null,
        onSelected: (Budget) -> Unit,
    ) {
        var expanded by remember { mutableStateOf(false) }
        var selectedId by remember { mutableIntStateOf(budgets[0].id) }
        @SuppressLint("UnrememberedMutableState")
        val selected by derivedStateOf {
            budgets.find { it.id == selectedId }
                ?: Budget(-1, "None", 0L)
        }

        Column {
            label?.let { it() }
            Card(
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                ExposedDropdownMenuBox(
                    modifier = Modifier,
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                ) {
                    ListItem(
                        modifier = Modifier.menuAnchor(),
                        headlineContent = { Text(selected.name) },
                        supportingContent = {
                            Text("\$${CurrencyVisualTransformation.transformText(selected.budgetAmount.toString())}")
                        },
                        leadingContent = {
                            Icon(
                                Icons.Filled.Info,
                                null
                            )
                        }
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        for (budget in budgets) {
                            DropdownMenuItem(
                                text = { Text(text = budget.name) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Info,
                                        null
                                    )
                                },
                                trailingIcon = { Text("\$${CurrencyVisualTransformation.transformText(budget.budgetAmount.toString())}") },
                                onClick = {
                                    selectedId = budget.id
                                    expanded = false
                                    onSelected(budget)
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }
            }

        }
    }

    /**
     * Composes a Text Input field for Currency.
     * Uses the numerical keyboard and a CurrencyVisualTransformation.
     *
     * @param onValueChange a callback called when the input text changes.
     */
    @Composable
    fun CurrencyAmount(onValueChange: (String) -> Unit) {
        var text by remember { mutableStateOf("") }

        OutlinedTextField(
            value = text,
            onValueChange = {
                if (it.isEmpty() || (it.toULongOrNull() != null) ) {
                    text = it
                    onValueChange(it)
                }
            },
            prefix = { Text("$") },
            singleLine = true,
            placeholder = { Text(text = "00.00") },
            label = { Text("Amount") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                autoCorrect = false,
            ),
            visualTransformation = currencyVisualTransformation()
        )
    }

    /**
     * A button labeled "Transfer".
     *
     * @param enabled whether the button is enabled.
     * @param onClick a callback called when the button is clicked.
     */
    @Composable
    fun TransactionButton(enabled: Boolean = true, onClick: () -> Unit) {
        Button(
            onClick = onClick,
            enabled = enabled,
        ) {
            Text(text = "Transfer")
        }
    }

    /**
     * A dialog for confirming a Transfer before completing it.
     *
     * @param onDismissRequest called when the user dismisses the dialog.
     * @param onConfirm called when the user confirms the transfer.
     * @param amount the amount of money to be transferred, in cents.
     * @param toAccount the name of the account the money will be transferred to.
     */
    @Composable
    fun ConfirmDialog(
        onDismissRequest: () -> Unit,
        onConfirm: () -> Unit,
        amount: Long,
        toAccount: String
    ) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text(text = "Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissRequest) {
                    Text(text = "cancel")
                }
            },
            title = { Text(text = "Confirm Transfer") },
            text = {
                Text(text = "Transfer \$${CurrencyVisualTransformation.transformText(
                    amount.toString()
                )} to ${toAccount}?")
            }
        )
    }

    /**
     * An infinite spinning dialog.
     * This dialog is not dismissable, and can only be removed
     *      by removing the Dialog from composition.
     */
    @Composable
    fun PendingDialog() {
        Dialog(onDismissRequest = { /* nothing, they have to wait */ }) {
            Card(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }

    /**
     * A Dialog confirming the Transfer was successful.
     *
     * @param onDismissRequest called when the user dismisses the dialog.
     */
    @Composable
    fun SuccessDialog(onDismissRequest: () -> Unit) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            confirmButton = {
                TextButton(onClick = onDismissRequest) {
                    Text(text = "Ok")
                }
            },
            title = { Text(text = "Success") },
            text = { Text(text = "Transfer Completed") },
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private enum class DialogState {
        NONE, CONFIRM, PENDING, SUCCESS
    }

}