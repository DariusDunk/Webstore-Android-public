package com.example.webstore_android_client.model.responses.review

import com.google.gson.annotations.SerializedName

data class ReviewContentResponse(
    @SerializedName("review_text")
    val reviewText: String,
    val rating: Short,
    val exists: Boolean
)
