/*
    This file defines the response object for when the access token is received from the backend server.
 */
package com.mobileapp.wisewallet.plaid

import com.google.gson.annotations.SerializedName

data class AccessTokenResponse(
    @SerializedName("access_token") val accessToken: String?,
    @SerializedName("item_id") val itemId: String?,
    @SerializedName("error") val error: String?
)