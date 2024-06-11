/*
    This file defines the response object for when transactions are received from the backend server.
    It receives a JSON array of transactions that are converted into a Kotlin list of transactions.
 */
package com.mobileapp.wisewallet.plaid

import com.google.gson.annotations.SerializedName

data class PlaidTransactionResponse(
    @SerializedName("latest_transactions")
    val addedTransactions: List<PlaidTransaction>
)

data class PlaidTransaction(
    @SerializedName("account_id")
    val accountId: String,
    @SerializedName("account_owner")
    val accountOwner: String?,
    val amount: Double,
    @SerializedName("authorized_date")
    val authorizedDate: String,
    @SerializedName("authorized_datetime")
    val authorizedDateTime: String?,
    val category: List<String>,
    @SerializedName("category_id")
    val categoryId: String,
    val counterparties: List<Counterparty>,
    val date: String,
    val datetime: String?,
    @SerializedName("iso_currency_code")
    val isoCurrencyCode: String,
    val location: Location,
    @SerializedName("logo_url")
    val logoUrl: String?,
    @SerializedName("merchant_entity_id")
    val merchantEntityId: String?,
    @SerializedName("merchant_name")
    val merchantName: String?,
    val name: String,
    @SerializedName("payment_channel")
    val paymentChannel: String,
    @SerializedName("payment_meta")
    val paymentMeta: PaymentMeta,
    val pending: Boolean,
    @SerializedName("pending_transaction_id")
    val pendingTransactionId: String?,
    @SerializedName("personal_finance_category")
    val personalFinanceCategory: PersonalFinanceCategory,
    @SerializedName("personal_finance_category_icon_url")
    val personalFinanceCategoryIconUrl: String?,
    @SerializedName("transaction_code")
    val transactionCode: String?,
    @SerializedName("transaction_id")
    val transactionId: String,
    @SerializedName("transaction_type")
    val transactionType: String,
    @SerializedName("unofficial_currency_code")
    val unofficialCurrencyCode: String?,
    val website: String?,
    @SerializedName("subtype")
    val subtype: String,
    @SerializedName("balance")
    val balances: Balances

)

data class Counterparty(
    @SerializedName("confidence_level")
    val confidenceLevel: String,
    @SerializedName("entity_id")
    val entityId: String?,
    @SerializedName("logo_url")
    val logoUrl: String?,
    val name: String,
    val type: String,
    val website: String?
)

data class Location(
    val address: String?,
    val city: String?,
    val country: String?,
    val lat: Double?,
    val lon: Double?,
    @SerializedName("postal_code")
    val postalCode: String?,
    val region: String?,
    @SerializedName("store_number")
    val storeNumber: String?
)

data class PaymentMeta(
    @SerializedName("by_order_of")
    val byOrderOf: String?,
    val payee: String?,
    val payer: String?,
    @SerializedName("payment_method")
    val paymentMethod: String?,
    @SerializedName("payment_processor")
    val paymentProcessor: String?,
    @SerializedName("ppd_id")
    val ppdId: String?,
    val reason: String?,
    @SerializedName("reference_number")
    val referenceNumber: String?
)

data class PersonalFinanceCategory(
    @SerializedName("confidence_level")
    val confidenceLevel: String,
    val detailed: String,
    val primary: String
)