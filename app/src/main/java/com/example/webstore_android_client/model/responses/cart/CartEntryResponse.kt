package com.example.webstore_android_client.model.responses.cart

import com.example.webstore_android_client.model.responses.product.CompactProductResponse
import com.google.gson.annotations.SerializedName
import java.time.Instant

data class CartEntryResponse(
    @SerializedName("product")
    val product: CompactProductResponse,
    @SerializedName("quantity")
    val quantity: Int,
    @SerializedName("stock_quantity")
    val stockQuantity: Int,
    @SerializedName("date_added")
    val dateAdded: Instant

) {
    val isDiscounted: Boolean
        get() = product.salePriceStotinki in 1 until product.originalPriceStotinki

    val currentPriceStotinki: Int
        get() = if (isDiscounted) product.salePriceStotinki else product.originalPriceStotinki

    val isOverStock: Boolean
        get() = quantity > stockQuantity
}