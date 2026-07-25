package com.example.webstore_android_client.model.responses.customer

import com.example.webstore_android_client.model.responses.cart.CartSummaryResponse
import com.google.gson.annotations.SerializedName

data class CustomerResponse(
    @SerializedName("username")
   val username: String,
    @SerializedName("customer_pfp")
    val customerPfp: String,
    @SerializedName("role")
   val role: String,
    @SerializedName("cartSummary")
    val cartSummary: CartSummaryResponse
)
