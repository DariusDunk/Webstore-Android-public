package com.example.webstore_android_client.model.responses.purchase

import com.google.gson.annotations.SerializedName

data class RecipientDataResponse(
    @SerializedName("contact_name")
    val contactName: String,
    @SerializedName("contact_number")
    val contactNumber: String,
    val address: String
)
