package com.example.webstore_android_client.model.responses.purchase

import com.example.webstore_android_client.model.responses.cart.CartSummaryResponse
import com.google.gson.annotations.SerializedName

data class SuccessfulPurchaseResponse(
    @SerializedName("purchase_code")
    val purchaseCode: String,
    @SerializedName("total_cost")
    val totalCost: Int,
    val status: String,
    @SerializedName("shipping_fee")
    val shippingFee: Int,
    @SerializedName("purchase_method")
    val purchaseMethod: String,
    val cartSummary: CartSummaryResponse
)
