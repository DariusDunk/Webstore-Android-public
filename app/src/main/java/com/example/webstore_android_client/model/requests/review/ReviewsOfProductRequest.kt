package com.example.webstore_android_client.model.requests.review

import com.google.gson.annotations.SerializedName

data class ReviewsOfProductRequest(
    @SerializedName("productCode")
    val productCode: String,
    @SerializedName("page")
    val page: Int,
    @SerializedName("sortOrder")
    val sortOrder: String,
    @SerializedName("verifiedOnly")
    val verifiedOnly: Boolean,
    @SerializedName("ratingValue")
    val ratingValue: Short?
)
