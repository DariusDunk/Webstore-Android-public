package com.example.webstore_android_client.model.responses.auth

import com.example.webstore_android_client.model.responses.cart.CartSummaryResponse
import com.google.gson.annotations.SerializedName

data class CombinedLoginResponse(
    @SerializedName("user")
    val loginResponse: LoginResponse,
    @SerializedName("cartSummary")
    val cartSummaryResponse: CartSummaryResponse
)