/*
    This is the viewmodel for the home fragment.
    It holds all the necessary API call info for plaid and rapid API.
    As well as all instances of the data base that was needed
 */
package com.mobileapp.wisewallet.ui.home
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.google.gson.Gson
import com.mobileapp.wisewallet.database.Account
import com.mobileapp.wisewallet.database.Stock
import com.mobileapp.wisewallet.database.Transaction
import com.mobileapp.wisewallet.database.WWDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
/**
 * ViewModel for managing the Home UI, holds application context and database instance
 */
class HomeViewModel(
    private val applicationContext: Context
) : ViewModel() {

    /**
     * @see database Database instance acquired from the WWDatabase singleton
     * @see accountsFlow LiveData flows for accounts and stocks, allowing reactive UI updates
     * @see stocksFlow LiveData flows for accounts and stocks, allowing reactive UI updates
     * @see stockDao DAO instances for accessing database operations for accounts
     * @see accountDao DAO instances for accessing database operations for stocks
     * @see transactionDao DAO instances for accessing database operations for accounts, stocks, and transactions
     * @see client HTTP client for network requests
     */
    private val database = WWDatabase.getInstance(applicationContext)

    val accountsFlow = database.accountDao().listenAccounts()

    val stockDao = database.stockDao()

    val accountDao = database.accountDao()

    val stocksFlow = database.stockDao().listenStocks()


    private val client = OkHttpClient()


    val transactionDao = database.transactionDao()
    private val _stockError = MutableLiveData<String>()
    val stockError: LiveData<String> = _stockError

    // Factory for creating ViewModel instances with Android's lifecycle
    companion object {
        val FACTORY = object : ViewModelProvider.Factory {

            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val applicationContext = checkNotNull(
                    extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                ).applicationContext

                @Suppress("UNCHECKED_CAST")
                return HomeViewModel(applicationContext) as T
            }
        }
    }
    /**
     * Fetches stock data from an API asynchronously, logging or inserting results in database
     *
     * @param symbol thats the symbol that the api searches for
     */
    fun fetchStockData(symbol: String) {
        viewModelScope.launch {
            val request = Request.Builder()
                .url("https://real-time-finance-data.p.rapidapi.com/stock-quote?symbol=${symbol}&language=en")
                .get()
                .addHeader("X-RapidAPI-Key", "b36254a3bbmsh67290b59a052be3p1c8af3jsn087b65312a00")
                .addHeader("X-RapidAPI-Host", "real-time-finance-data.p.rapidapi.com")
                .build()

            withContext(Dispatchers.IO) {
                try {
                    val response = client.newCall(request).execute()
                    val responseBody = response.body?.string()

                    if (responseBody != null) {
                        Log.i("STOCKAPIthisone", responseBody)
                        val stockTransactionResponse = parseStockTransactionResponse(responseBody)
                        if (stockTransactionResponse != null && stockTransactionResponse.status == "OK") {
                            stockDao.insert(stockTransactionResponse.data.toStock())
                        } else {
                            _stockError.postValue("Invalid stock symbol: $symbol")
                            Log.i("STOCKAPI", "Invalid or no data for symbol: $symbol")
                        }
                    } else {
                        _stockError.postValue("Failed to fetch data for symbol: $symbol")
                        Log.e("STOCKAPI", "Failed to fetch or parse data")
                    }
                } catch (e: Exception) {
                    _stockError.postValue("Error fetching data: ${e.message}")
                    Log.e("STOCKAPI", "Exception during API call", e)
                }
            }
        }
    }
    /**
     * Parses the JSON response for stock transactions
     *
     * @param jsonString body from api request
     */
    private fun parseStockTransactionResponse(jsonString: String): StockTransactionResponse? {
        return try {
            Gson().fromJson(jsonString, StockTransactionResponse::class.java)
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Failed to parse JSON", e)
            null
        }
    }

    /**
     * Inserts an Account into the database, returning the newly-generated ID.
     *
     * @param account the new Account to be inserted.
     * @return the new ID of the inserted Account.
     */
    suspend fun insertAndGetAccount(account: Account): Long {
        return accountDao.insertOne(account)
    }
    /**
     * Asynchronously inserts an account into the database
     *
     * @param account new Account to be inserted into DB
     */
    fun insertAccount(account: Account) {
        viewModelScope.launch { accountDao.insert(account) }
    }

    /**
     * Asynchronously deletes an account and its related transactions from the database
     *
     * @param account Account to be deleted from DB
     */
    fun deleteAccount(account: Account) {
        viewModelScope.launch {
            accountDao.delete(account)
            transactionDao.deleteTransactionsBySourceId(account.id)

        }
    }
    /**
     * Asynchronously inserts a transaction into the database
     *
     * @param transaction Transaction to be added to DB
     */
    fun insertTransaction(transaction: Transaction) {
        viewModelScope.launch { transactionDao.insert(transaction) }
    }
    /**
     * Asynchronously deletes a stock from the database
     *
     * @param account Account to be deleted from DB
     */
    fun deleteStock(stock: Stock) {
        viewModelScope.launch { stockDao.delete(stock) }
    }



}