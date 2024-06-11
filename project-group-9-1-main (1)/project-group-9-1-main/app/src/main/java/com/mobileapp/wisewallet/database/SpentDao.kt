// Spent Dao used to store spent data that can store the id, name, and spentAmount of the spent data
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
 * Represents a Spent stored in the Database.
 */
@Entity(tableName = "spents")
data class Spent(

    /**
     * Represents a non-deterministically-generated ID for this Spent.
     * This ID will be numerically greater than 0.
     */
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    /**
     * Represents the name of this Spent.
     */
    val name: String,
    val spentAmount: Long,
) {
    constructor(
        spentAmount:Long
    ) : this(0, "",spentAmount)
}

@Dao
interface SpentDao {

    /**
     * Inserts one or more Spent into the Database
     *
     * Note: In order to properly auto-generate an ID,
     * the ID of each provided Spent should be 0.
     *
     * @param spent the Spent(s) to insert, with the ID set to 0.
     */
    @Insert
    suspend fun insert(vararg spent: Spent)
    /**
     * Inserts one or more Spent into the Database
     *
     * Note: In order to properly auto-generate an ID,
     * the ID of each provided Spent should be 0.
     *
     * Note: This function is blocking, and may not be called from the main thread.
     *
     * @param spent the Spent(s) to insert, with the ID set to 0.
     */
    @Insert
    fun insertBlocking(vararg spent: Spent)

    /**
     * Updates one or more Spent in the Database.
     * The provided Spent's ID must match the Spent to replace.
     *
     * @param spent the Spent(s) to update.
     */
    @Update
    suspend fun update(vararg spent: Spent)

    /**
     * Updates one or more Spent in the Database.
     * The provided Spent's ID must match the Spent to replace.
     *
     * Note: This function is blocking, and may not be called from the main thread.
     *
     * @param spent the Spent(s) to update.
     */
    @Update
    fun updateBlocking(vararg spent: Spent)

    /**
     * Removes one or more Spent from the Database.
     * The provided Spent's IDs must match the Spent to remove.
     *
     * @param spent the Spent(s) to remove.
     */
    @Delete
    suspend fun delete(vararg spent: Spent)
    /**
     * Removes all Spent from the Database.
     * The provided Spent's IDs must match the Spent to remove.
     *
     */
    @Query("DELETE FROM spents")
    suspend fun deleteAll()

    /**
     * Removes one or more Spent from the Database.
     * The provided Spent's IDs must match the Spent to remove.
     *
     * Note: This function is blocking, and may not be called from the main thread.
     *
     * @param spent the Spent(s) to remove.
     */
    @Delete
    fun deleteBlocking(vararg spent: Spent)

    /**
     * Retrieves all Spent from the Database.
     *
     * @return a List of Spent from the Database.
     */
    @Query("SELECT * FROM `spents`")
    suspend fun getspents(): List<Spent>

    /**
     * Retrieves all Spent from the Database.
     *
     * Note: This function is blocking, and may not be called from the main thread.
     *
     * @return a List of Spent from the Database.
     */
    @Query("SELECT * FROM `spents`")
    fun getspentsBlocking(): List<Spent>

    /**
     * Retrieves all Spent from the Database.
     * This function returns a Flow, which can provide asynchronous updates.
     *
     * @return a Flow providing Lists of Spent from the Database.
     */
    @Query("SELECT * FROM `spents`")
    fun listenspents(): Flow<List<Spent>>

    /**
     * Retrieves all Spent from the Database.
     * Spent are sorted in alphabetical order.
     *
     * @return a List of Spent from the Database.
     */
    @Query("SELECT * FROM `spents` ORDER BY `name` ASC")
    suspend fun getspentsAlphabetical(): List<Spent>

    /**
     * Retrieves all Spent from the Database.
     * Spent are sorted in alphabetical order.
     *
     * Note: This function is blocking, and may not be called from the main thread.
     *
     * @return a List of Spent from the Database.
     */
    @Query("SELECT * FROM `spents` ORDER BY `name` ASC")
    fun getspentsAlphabeticalBlocking(): List<Spent>
    @Query("SELECT * FROM `spents` ORDER BY `name` ASC")
    fun listenspentsAlphabetical(): Flow<List<Spent>>

}