package com.mobileapp.wisewallet.ui.home

import com.google.gson.annotations.SerializedName
import com.mobileapp.wisewallet.database.Stock

data class StockTransactionResponse(
    val status: String,
    val requestId: String,
    @SerializedName("data")
    val data: StockData
)

data class StockData(
    @SerializedName("symbol")
    val symbol: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("price")
    val price: Double,
    @SerializedName("open")
    val open: Double,
    @SerializedName("high")
    val high: Double,
    @SerializedName("low")
    val low: Double,
    @SerializedName("volume")
    val volume: Long,
    val previousClose: Double,
    @SerializedName("change")
    val change: Double,
    val changePercent: Double,
    val preOrPostMarket: Double,
    val preOrPostMarketChange: Double,
    val preOrPostMarketChangePercent: Double,
    val lastUpdateUtc: String
)
fun StockData.toStock(): Stock {
    return Stock(
        symbol = this.symbol,
        name = this.name,
        price = this.price,
        open = this.open,
        high = this.high,
        low = this.low,
        volume = this.volume,
        change = this.change
    )
}
