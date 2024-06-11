package com.mobileapp.wisewallet.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Represents a Transaction stored in the Database.
 */
@Entity(tableName = "transactions")
data class Transaction(

    /**
     * Represents a non-deterministically-generated ID for this Transaction.
     * This ID will be numerically greater than 0.
     */
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    /**
     * Represents a brief description of this Transaction.
     */
    val description: String,
    /**
     * Represents the ID of the Account or Card this
     *      transaction originated from.
     */
    val sourceId: Int,
    /**
     * Represents the type of the source referenced by `sourceId`.
     * If the value is `SOURCE_ACCOUNT`, this is an Account.
     * If the value is `SOURCE_CARD`, this is a Card.
     */
    val sourceType: Int,
    /**
     * Represents the value of this Transaction, in cents.
     * If this value is positive, this represents an amount
     *      removed from the specified Account.
     * If this value is negative, this represents an amount
     *      deposited into the specified Account.
     */
    val amount: Long,
    /**
     * Represents the date and time this Transaction was made.
     * This should be expressed in ISO Date/Time format
     *      (YYYY-MM-DDTHH:MM:SS).
     */
    val date: String,
    /**
     * Represents the ID of the Budget this Transaction should be
     *      associated with, if any.
     * If no Budget is associated with this Transaction, this value is `null`.
     */
    val budgetId: Int?,
) {
    /**
     * Creates a new Transaction with the ID initialized to 0.
     */
    constructor(
        description: String,
        sourceId: Int,
        sourceType: Int,
        amount: Long,
        date: String,
        budgetId: Int?,
    ) : this(0, description, sourceId, sourceType, amount, date, budgetId)
    companion object {

        /**
         * A `sourceType` representing an Account.
         */
        const val SOURCE_ACCOUNT = 1
        /**
         * A `sourceType` representing a Card.
         */
        const val SOURCE_CARD = 2
    }
}

@Dao
interface TransactionDao {

    /**
     * Inserts one or more Transactions into the Database
     *
     * Note: In order to properly auto-generate an ID,
     * the ID of each provided Transaction should be 0.
     *
     * @param transaction the Transaction(s) to insert, with the ID set to 0.
     */
    @Insert
    suspend fun insert(vararg transaction: Transaction)
    /**
     * Inserts one or more Transactions into the Database
     *
     * Note: In order to properly auto-generate an ID,
     * the ID of each provided Transaction should be 0.
     *
     * Note: This function is blocking, and may not be called from the main thread.
     *
     * @param transaction the Transaction(s) to insert, with the ID set to 0.
     */
    @Insert
    fun insertBlocking(vararg transaction: Transaction)

    /**
     * Updates one or more Transactions in the Database.
     * The provided Transaction's ID must match the Transaction to replace.
     *
     * @param transaction the Transaction(s) to update.
     */
    @Update
    suspend fun update(vararg transaction: Transaction)
    /**
     * Updates one or more Transactions in the Database.
     * The provided Transaction's ID must match the Transaction to replace.
     *
     * Note: This function is blocking, and may not be called from the main thread.
     *
     * @param transaction the Transaction(s) to update.
     */
    @Update
    fun updateBlocking(vararg transaction: Transaction)

    /**
     * Removes one or more Transactions from the Database.
     * The provided Transaction's IDs must match the Transactions to remove.
     *
     * @param transaction the Transaction(s) to remove.
     */
    @Delete
    suspend fun delete(vararg transaction: Transaction)
    /**
     * Removes one or more Transactions from the Database.
     * The provided Transaction's IDs must match the Transactions to remove.
     *
     * Note: This function is blocking, and may not be called from the main thread.
     *
     * @param transaction the Transaction(s) to remove.
     */
    @Delete
    fun deleteBlocking(vararg transaction: Transaction)

    /**
     * Retrieves all Transactions from the Database.
     *
     * @return a List of Transactions from the Database.
     */
    @Query("SELECT * FROM `transactions`")
    suspend fun getTransactions(): List<Transaction>
    /**
     * Retrieves all Transactions from the Database.
     *
     * Note: This function is blocking, and may not be called from the main thread.
     *
     * @return a List of Transactions from the Database.
     */
    @Query("SELECT * FROM `transactions`")
    fun getTransactionsBlocking(): List<Transaction>
    /**
     * Retrieves all Transactions from the Database.
     * This function returns a Flow, which can provide asynchronous updates.
     *
     * @return a List of Transactions from the Database.
     */
    @Query("SELECT * FROM `transactions`")
    fun listenTransactions(): Flow<List<Transaction>>
    @Query("DELETE FROM transactions WHERE sourceId = :sourceId")
    suspend fun deleteTransactionsBySourceId(sourceId: Int)

}