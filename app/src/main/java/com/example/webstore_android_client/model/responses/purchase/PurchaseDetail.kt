package com.example.webstore_android_client.model.responses.purchase

import com.google.gson.annotations.SerializedName
import java.time.Instant

data class PurchaseDetailResponse(
    val products: List<DetailedPurchaseProductResponse>,
    @SerializedName("products_total")
    val productsTotalCents: Int,
    @SerializedName("recipient_name")
    val recipientName: String?,
    @SerializedName("recipient_phone")
    val recipientPhone: String?,
    @SerializedName("payment_method")
    val paymentMethod: String?,
    @SerializedName("delivery_date")
    val deliveryDate: Instant?,
)
