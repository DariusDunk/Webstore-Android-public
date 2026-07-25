package com.example.webstore_android_client.model.requests.purchase

import com.google.gson.annotations.SerializedName

data class PurchaseProduct(
    @SerializedName("product_code")
    val productCode: String,
    val quantity: Int
)
