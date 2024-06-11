package com.mobileapp.wisewallet.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * The main Database for WiseWallet persistent data.
 *
 * To interact with the database, obtain an instance with getInstance.
 */
@Database(entities = [
        Account::class,
        Card::class,
        Transaction::class,
        Budget::class,
        Spent::class,
        Stock::class,
], version = 8)
abstract class WWDatabase: RoomDatabase() {

    /**
     * Returns a Database Access Object for Accounts.
     *
     * @return a new Database Access Object.
     */
    abstract fun accountDao(): AccountDao

    /**
     * Returns a Database Access Object for Cards.
     *
     * @return a new Database Access Object.
     */
    abstract fun cardDao(): CardDao

    /**
     * Returns a Database Access Object for Transactions.
     *
     * @return a new Database Access Object.
     */
    abstract fun transactionDao(): TransactionDao

    /**
     * Returns a Database Access Object for Budgets.
     *
     * @return a new Database Access Object.
     */
    abstract fun budgetDao(): BudgetDao
    abstract fun spentDao(): SpentDao

    abstract fun stockDao(): StockDao

    companion object {
        /**
         * Returns a new instance of the WiseWallet Database.
         *
         * @param context a WiseWallet application or fragment Context object
         * @return an instance of the WiseWallet Database.
         */
        fun getInstance(context: Context): WWDatabase {
            return Room.databaseBuilder(
                context,
                WWDatabase::class.java,
                "wisewallet"
            )
            .createFromAsset("sample_database.db")
            .enableMultiInstanceInvalidation()
            .fallbackToDestructiveMigration()
            .build()

        }
    }
}