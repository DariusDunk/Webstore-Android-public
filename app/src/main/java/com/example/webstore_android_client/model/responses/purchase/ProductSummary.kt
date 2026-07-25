package com.example.webstore_android_client.model.responses.purchase

import com.google.gson.annotations.SerializedName

data class ProductSummary(
    @SerializedName("product_code")
    val productCode: String,
    @SerializedName("product_name")
    val productName: String,
    @SerializedName("image_url")
    val imageUrl: String,
)
