package com.example.webstore_android_client.model.requests.purchase

import com.google.gson.annotations.SerializedName

data class PurchaseCompleteRequest(
    val products: List<PurchaseProduct>,
    @SerializedName("is_direct_purchase")
    val isDirectPurchase: Boolean,
    @SerializedName("recipientData")
    val recipientDataRequest: RecipientDataRequest,
    @SerializedName("payment_method")
    val paymentMethod: String,
    val email: String
)
