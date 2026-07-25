package com.example.webstore_android_client.model.responses.review

import com.google.gson.annotations.SerializedName

data class CustomerDetailsForReview(
    @SerializedName("name") val name: String,
    @SerializedName("customerPfp") val customerPfp: String?,
    @SerializedName("isVerified") val isVerified: Boolean,
    @SerializedName("currentUser") val currentUser: Boolean,
    @SerializedName("is_expired") val isExpired: Boolean
)
