package com.example.webstore_android_client.model.responses.cart

import com.google.gson.annotations.SerializedName

data class MegaCartActionResponse(
    @SerializedName("products")
    val products: List<CartEntryResponse>,
    @SerializedName("cartSummary")
    val cartSummaryResponse: CartSummaryResponse
)
