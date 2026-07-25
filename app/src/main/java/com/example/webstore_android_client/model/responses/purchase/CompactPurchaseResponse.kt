package com.example.webstore_android_client.model.responses.purchase

import com.google.gson.annotations.SerializedName
import java.time.Instant

data class CompactPurchaseResponse(
    @SerializedName("purchase_code")
    val purchaseCode: String,
    @SerializedName("purchase_date")
    val purchaseDate: Instant?,
    val status: String,
    val products: List<ProductSummary>,
    @SerializedName("delivery_address")
    val deliveryAddress: String?,
    @SerializedName("total_cost")
    val totalCostCents: Int,
    @SerializedName("shipping_fee")
    val shippingFeeCents: Int,
)
