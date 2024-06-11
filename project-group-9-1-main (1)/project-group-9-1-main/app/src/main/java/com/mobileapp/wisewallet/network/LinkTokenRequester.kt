/*
    This file implements the client side communication logic for our AWS server. It sets the IP address
    for the server, and configures an HTTP Client using the retrofit library to deserialize JSON files and
    set a framework for handling HTTP responses. OkHTTP is used to build the client.

    This file is adapted from the "plaid-link-android" repository that was created by Plaid to assist
    with the creation of Android apps that use the Plaid API.
    Link: https://github.com/plaid/plaid-link-android
 */
package com.mobileapp.wisewallet.network

import io.reactivex.Scheduler
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object LinkTokenRequester {
    private val baseUrl = "http://18.118.222.172:8000" // AWS server address

    val retrofit: Retrofit = Retrofit.Builder() // HTTP Client
        .baseUrl(baseUrl)
        .client(OkHttpClient.Builder().build())
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .build()

    val api = retrofit.create(LinkSampleApi::class.java) // API object with HTTP Configuration

    val token
        get() = api.getLinkToken()            // Async Getter
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { it.link_token }
}

