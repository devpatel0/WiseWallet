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
 * Represents a Card stored in the Database.
 */
@Entity(tableName = "cards")
data class Card(

    /**
     * Represents a non-deterministically-generated ID for this Card.
     * This ID will be numerically greater than 0.
     */
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    /**
     * Represents the name of this Card.
     */
    val name: String,
    /**
     * Represents the expiration date, as a String.
     */
    val exp: String,
    /**
     * The last 4 digits of the Card number, as an integer.
     */
    val digits: Int,
    /**
     * Represents the current balance of this Card, in cents.
     */
    val balance: Long,
    /**
     * Represents whether this Card is backed by a third-party account.
     * Cards linked with a third-party (ex. Plaid) should set this to true.
     */
    val backed: Boolean,
){
    /**
     * Creates a new Card with the ID initialized to 0.
     */
    constructor(
        name: String,
        exp: String,
        digits: Int,
        balance: Long,
        backed: Boolean,
    ): this(0, name, exp, digits, balance, backed)

}

@Dao
interface CardDao {

    /**
     * Inserts one or more Cards into the Database
     *
     * Note: In order to properly auto-generate an ID,
     * the ID of each provided Card should be 0.
     *
     * @param card the Card(s) to insert, with the ID set to 0.
     */
    @Insert
    suspend fun insert(vararg card: Card)
    /**
     * Inserts one or more Cards into the Database
     *
     * Note: In order to properly auto-generate an ID,
     * the ID of each provided Card should be 0.
     *
     * Note: This function is blocking, and may not be called from the main thread.
     *
     * @param card the Card(s) to insert, with the ID set to 0.
     */
    @Insert
    fun insertBlocking(vararg card: Card)

    /**
     * Updates one or more Cards in the Database.
     * The provided Card's ID must match the Card to replace.
     *
     * @param card the Card(s) to update.
     */
    @Update
    suspend fun update(vararg card: Card)
    /**
     * Updates one or more Cards in the Database.
     * The provided Card's ID must match the Card to replace.
     *
     * Note: This function is blocking, and may not be called from the main thread.
     *
     * @param card the Card(s) to update.
     */
    @Update
    fun updateBlocking(vararg card: Card)

    /**
     * Removes one or more Cards from the Database.
     * The provided Card's IDs must match the Cards to remove.
     *
     * @param card the Card(s) to remove.
     */
    @Delete
    suspend fun delete(vararg card: Card)
    /**
     * Removes one or more Cards from the Database.
     * The provided Card's IDs must match the Cards to remove.
     *
     * Note: This function is blocking, and may not be called from the main thread.
     *
     * @param card the Card(s) to remove.
     */
    @Delete
    fun deleteBlocking(vararg card: Card)

    /**
     * Retrieves all Cards from the Database.
     *
     * @return a List of Cards from the Database.
     */
    @Query("SELECT * FROM `cards`")
    suspend fun getCards(): List<Card>
    /**
     * Retrieves all Cards from the Database.
     *
     * Note: This function is blocking, and may not be called from the main thread.
     *
     * @return a List of Cards from the Database.
     */
    @Query("SELECT * FROM `cards`")
    fun getCardsBlocking(): List<Card>
    /**
     * Retrieves all Cards from the Database.
     * This function returns a Flow, which can provide asynchronous updates.
     *
     * @return a Flow providing Lists of Cards from the Database.
     */
    @Query("SELECT * FROM `cards`")
    fun listenCards(): Flow<List<Card>>
}



