package com.example.webstore_android_client.model.requests.purchase

import com.google.gson.annotations.SerializedName

data class RecipientDataRequest(
    @SerializedName("contact_name")
    val contactName: String,
    @SerializedName("contact_number")
    val contactNumber: String,
    @SerializedName("address")
    val address: String
)
