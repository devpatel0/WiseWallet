/*
    This file defines the response object for when the balance is received from the backend server.
 */
package com.mobileapp.wisewallet.plaid

import com.google.gson.annotations.SerializedName

data class BalanceResponse(
    @SerializedName("accounts")
    val accounts: List<Account>,
    @SerializedName("item")
    val item: Item,
    @SerializedName("request_id")
    val requestId: String
)

data class Account(
    @SerializedName("account_id")
    val accountId: String,
    val balances: Balances,
    val mask: String,
    val name: String,
    @SerializedName("official_name")
    val officialName: String,
    @SerializedName("persistent_account_id")
    val persistentAccountId: String,
    val subtype: String,
    val type: String
)

data class Balances(
    val available: Double,
    val current: Double,
    @SerializedName("iso_currency_code")
    val isoCurrencyCode: String,
    val limit: Double?,
    @SerializedName("unofficial_currency_code")
    val unofficialCurrencyCode: String?
)

data class Item(
    @SerializedName("available_products")
    val availableProducts: List<String>,
    @SerializedName("billed_products")
    val billedProducts: List<String>,
    @SerializedName("consent_expiration_time")
    val consentExpirationTime: String?,
    val error: String?,
    @SerializedName("institution_id")
    val institutionId: String,
    @SerializedName("item_id")
    val itemId: String,
    val products: List<String>,
    @SerializedName("update_type")
    val updateType: String,
    val webhook: String?
)