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
 * Represents a Budget stored in the Database.
 */
@Entity(tableName = "budgets")
data class Budget(

    /**
     * Represents a non-deterministically-generated ID for this Budget.
     * This ID will be numerically greater than 0.
     */
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    /**
     * Represents the name of this Budget.
     */
    val name: String,

    val budgetAmount: Long,
) {
    constructor(
        budgetAmount:Long
    ) : this(0, "",budgetAmount)
}


@Dao
interface BudgetDao {

    /**
     * Inserts one or more Budgets into the Database
     *
     * Note: In order to properly auto-generate an ID,
     * the ID of each provided Budget should be 0.
     *
     * @param budget the Budget(s) to insert, with the ID set to 0.
     */
    @Insert
    suspend fun insert(vararg budget: Budget)
    /**
     * Inserts one or more Budgets into the Database
     *
     * Note: In order to properly auto-generate an ID,
     * the ID of each provided Budget should be 0.
     *
     * Note: This function is blocking, and may not be called from the main thread.
     *
     * @param budget the Budget(s) to insert, with the ID set to 0.
     */
    @Insert
    fun insertBlocking(vararg budget: Budget)

    /**
     * Updates one or more Budgets in the Database.
     * The provided Budget's ID must match the Budget to replace.
     *
     * @param budget the Budget(s) to update.
     */
    @Update
    suspend fun update(vararg budget: Budget)
    /**
     * Updates one or more Budgets in the Database.
     * The provided Budget's ID must match the Budget to replace.
     *
     * Note: This function is blocking, and may not be called from the main thread.
     *
     * @param budget the Budget(s) to update.
     */
    @Update
    fun updateBlocking(vararg budget: Budget)

    /**
     * Removes one or more Budgets from the Database.
     * The provided Budget's IDs must match the Budgets to remove.
     *
     * @param budget the Budget(s) to remove.
     */
    @Delete
    suspend fun delete(vararg budget: Budget)
    @Query("DELETE FROM budgets")
    suspend fun deleteAll()
    /**
     * Removes one or more Budgets from the Database.
     * The provided Budget's IDs must match the Budgets to remove.
     *
     * Note: This function is blocking, and may not be called from the main thread.
     *
     * @param budget the Budget(s) to remove.
     */
    @Delete
    fun deleteBlocking(vararg budget: Budget)

    /**
     * Retrieves all Budgets from the Database.
     *
     * @return a List of Budgets from the Database.
     */
    @Query("SELECT * FROM `budgets`")
    suspend fun getBudgets(): List<Budget>
    /**
     * Retrieves all Budgets from the Database.
     *
     * Note: This function is blocking, and may not be called from the main thread.
     *
     * @return a List of Budgets from the Database.
     */
    @Query("SELECT * FROM `budgets`")
    fun getBudgetsBlocking(): List<Budget>
    /**
     * Retrieves all Budgets from the Database.
     * This function returns a Flow, which can provide asynchronous updates.
     *
     * @return a Flow providing Lists of Budgets from the Database.
     */
    @Query("SELECT * FROM `budgets`")
    fun listenBudgets(): Flow<List<Budget>>

    /**
     * Retrieves all Budgets from the Database.
     * Budgets are sorted in alphabetical order.
     *
     * @return a List of Budgets from the Database.
     */
    @Query("SELECT * FROM `budgets` ORDER BY `name` ASC")
    suspend fun getBudgetsAlphabetical(): List<Budget>
    /**
     * Retrieves all Budgets from the Database.
     * Budgets are sorted in alphabetical order.
     *
     * Note: This function is blocking, and may not be called from the main thread.
     *
     * @return a List of Budgets from the Database.
     */
    @Query("SELECT * FROM `budgets` ORDER BY `name` ASC")
    fun getBudgetsAlphabeticalBlocking(): List<Budget>
    /**
     * Retrieves all Budgets from the Database.
     * Budgets are sorted in alphabetical order.
     * This function returns a Flow, which can provide asynchronous updates.
     *
     * @return a Flow providing Lists of Budgets from the Database.
     */
    @Query("SELECT * FROM `budgets` ORDER BY `name` ASC")
    fun listenBudgetsAlphabetical(): Flow<List<Budget>>

    //To add values to the budget amount
    @Query("UPDATE budgets SET budgetAmount = budgetAmount + :amountToAdd WHERE id = :budgetId")
    suspend fun updateBudgetAmount(budgetId: Int, amountToAdd: Long)
}