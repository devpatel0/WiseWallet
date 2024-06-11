package com.mobileapp.wisewallet.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
/**
 * Represents an Stock stored in the Database.
 */
@Entity(tableName = "stocks")
data class Stock(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val symbol: String,
    val name: String,
    val price: Double,
    val open: Double,
    val high: Double,
    val low: Double,
    val volume: Long,
    val change: Double
){
    constructor(
        symbol: String,
        name: String,
        price: Double,
        open: Double,
        high: Double,
        low: Double,
        volume: Long,
        change: Double
    ):this(0, symbol, name, price, open, high, low, volume, change)
}
@Dao
interface StockDao {

    @Insert
    suspend fun insert(vararg stock: Stock)

    @Delete
    suspend fun delete(vararg stock: Stock)

    @Query("SELECT * FROM 'stocks' WHERE symbol = :symbol")
    suspend fun getStockBySymbol(symbol: String): Stock?

    @Query("SELECT * FROM `stocks`")
    fun listenStocks(): Flow<List<Stock>>
}
