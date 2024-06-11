/*
    This file defines the endpoints for the Plaid API that can be requested from the backend server.
    It also defines the link token object that will be stored while Plaid accounts are being connected.

    This file is adapted from the "plaid-link-android" repository that was created by Plaid to assist
    with the creation of Android apps that use the Plaid API.
    Link: https://github.com/plaid/plaid-link-android
 */
package com.mobileapp.wisewallet.network

import com.google.gson.annotations.SerializedName
import com.mobileapp.wisewallet.plaid.AccessTokenResponse
import com.mobileapp.wisewallet.plaid.BalanceResponse
import com.mobileapp.wisewallet.plaid.PlaidTransactionResponse
import io.reactivex.rxjava3.core.Single
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST


interface LinkSampleApi {

    @POST("/api/create_link_token")
    fun getLinkToken(): Single<LinkToken>

    @POST("/api/set_access_token")
    @FormUrlEncoded
    fun setAccessToken(@Field("public_token") publicToken: String): Call<AccessTokenResponse>

    @GET("/api/transactions")
    fun getTransactions(): Call<PlaidTransactionResponse>

    @GET("/api/balance")
    fun getBalance(): Call<BalanceResponse>
}

data class LinkToken(
    @SerializedName("link_token") val link_token: String
)

