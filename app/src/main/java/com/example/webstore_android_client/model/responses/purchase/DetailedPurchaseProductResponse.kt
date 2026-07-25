package com.example.webstore_android_client.model.responses.purchase

import com.example.webstore_android_client.ui.userProfile.purchaseHistory.OrderProduct
import com.google.gson.annotations.SerializedName

data class DetailedPurchaseProductResponse(
    @SerializedName("product_code")
    val productCode: String,
    @SerializedName("product_name")
    val productName: String,
    @SerializedName("image_url")
    val imageUrl: String,
    val rating: Int,
    @SerializedName("review_count")
    val reviewCount: Int,
    @SerializedName("single_price")
    val singlePriceCents: Int,
    val quantity: Int,
) {

    fun toOrderProduct(): OrderProduct {
        return OrderProduct(
            productCode = productCode,
            productName = productName,
            imageUrl = imageUrl,
            rating = rating,
            reviewCount = reviewCount,
            singlePriceCents = singlePriceCents,
            quantity = quantity,
        )

    }

}