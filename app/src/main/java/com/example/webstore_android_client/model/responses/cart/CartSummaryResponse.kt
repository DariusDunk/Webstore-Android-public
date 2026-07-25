package com.example.webstore_android_client.model.responses.cart

import com.google.gson.annotations.SerializedName

data class CartSummaryResponse(
    @SerializedName("cart_total")
    val cartTotalCoins: Long,
    @SerializedName("cart_quantity")
    val cartQuantity: Long
)
