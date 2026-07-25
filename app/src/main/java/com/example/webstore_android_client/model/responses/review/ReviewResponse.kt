package com.example.webstore_android_client.model.responses.review

import com.google.gson.annotations.SerializedName
import java.time.Instant

data class ReviewResponse(
    @SerializedName("reviewId") val reviewId: Long,
    @SerializedName("reviewText") val reviewText: String,
    @SerializedName("rating") val rating: Short,
    @SerializedName("post_timestamp") val postTimestamp: Instant,
    @SerializedName("customerDetailsForReview") val customerDetailsForReview: CustomerDetailsForReview,
    @SerializedName("is_deleted") val isDeleted: Boolean
)
