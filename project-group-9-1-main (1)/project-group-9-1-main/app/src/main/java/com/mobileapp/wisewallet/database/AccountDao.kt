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
 * Represents an Account stored in the Database.
 */
@Entity(tableName = "accounts")
data class Account(

    /**
     * Represents a non-deterministically-generated ID for this Account.
     * This ID will be numerically greater than 0.
     */
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    /**
     * Represents the name of this Account.
     */
    val name: String,
    /**
     * Represents a brief description of this Account.
     */
    val type: String,
    /**
     * Represents the current balance of this Account, in cents.
     */
    val balance: Long,
    /**
     * Represents whether this Account is backed by a third-party account.
     * Accounts linked with a third-party (ex. Plaid) should set this to true.
     */
    val backed: Boolean,
) {
    /**
     * Creates a new Account with the ID initialized to 0.
     */
    constructor(
        name: String,
        type: String,
        balance: Long,
        backed: Boolean,
    ): this(0, name, type, balance, backed)
}

@Dao
interface AccountDao {

    /**
     * Inserts an Account into the Database.
     *
     * Note: In order to properly auto-generate an ID,
     * the ID of the provided account should be 0.
     *
     * @param account the account to insert, with the ID set to 0.
     * @return the newly-generated ID of the inserted Account.
     */
    @Insert
    suspend fun insertOne(account: Account): Long
    /**
     * Inserts an Account into the Database.
     *
     * Note: In order to properly auto-generate an ID,
     * the ID of the provided account should be 0.
     *
     * Note: This function is blocking, and may not be called from the main thread.
     *
     * @param account the account to insert, with the ID set to 0.
     * @return the newly-generated ID of the inserted Account.
     */
    @Insert
    fun insertOneBlocking(account: Account): Long

    /**
     * Inserts one or more Accounts into the Database
     *
     * Note: In order to properly auto-generate an ID,
     * the ID of each provided account should be 0.
     *
     * @param account the account(s) to insert, with the ID set to 0.
     */
    @Insert
    suspend fun insert(vararg account: Account)
    /**
     * Inserts one or more Accounts into the Database
     *
     * Note: In order to properly auto-generate an ID,
     * the ID of each provided account should be 0.
     *
     * Note: This function is blocking, and may not be called from the main thread.
     *
     * @param account the account(s) to insert, with the ID set to 0.
     */
    @Insert
    fun insertBlocking(vararg account: Account)

    /**
     * Updates one or more Accounts in the Database.
     * The provided Account's ID must match the Account to replace.
     *
     * @param account the account(s) to update.
     */
    @Update
    suspend fun update(vararg account: Account)
    /**
     * Updates one or more Accounts in the Database.
     * The provided Account's ID must match the Account to replace.
     *
     * Note: This function is blocking, and may not be called from the main thread.
     *
     * @param account the account(s) to update.
     */
    @Update
    fun updateBlocking(vararg account: Account)

    /**
     * Removes one or more Accounts from the Database.
     * The provided Account's IDs must match the Accounts to remove.
     *
     * @param account the account(s) to remove.
     */
    @Delete
    suspend fun delete(vararg account: Account)
    /**
     * Removes one or more Accounts from the Database.
     * The provided Account's IDs must match the Accounts to remove.
     *
     * Note: This function is blocking, and may not be called from the main thread.
     *
     * @param account the account(s) to remove.
     */
    @Delete
    fun deleteBlocking(vararg account: Account)

    /**
     * Retrieves all Accounts from the Database.
     *
     * @return a List of Accounts from the Database.
     */
    @Query("SELECT * FROM `accounts`")
    suspend fun getAccounts(): List<Account>
    /**
     * Retrieves all Accounts from the Database.
     *
     * Note: This function is blocking, and may not be called from the main thread.
     *
     * @return a List of Accounts from the Database.
     */
    @Query("SELECT * FROM `accounts`")
    fun getAccountsBlocking(): List<Account>
    /**
     * Retrieves all Accounts from the Database.
     * This function returns a Flow, which can provide asynchronous updates.
     *
     * @return a Flow providing Lists of Accounts from the Database.
     */
    @Query("SELECT * FROM `accounts`")
    fun listenAccounts(): Flow<List<Account>>
}





