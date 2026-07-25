package com.example.webstore_android_client.model.requests.review

data class ReviewPostRequest(
    val rating: Short,
    val reviewText: String,
    val productCode: String
)
