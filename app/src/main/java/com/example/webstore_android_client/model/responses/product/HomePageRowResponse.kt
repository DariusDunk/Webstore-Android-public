package com.example.webstore_android_client.model.responses.product

data class HomePageRowResponse(
    val type: String,
    val title: String,
    val products: List<CompactProductResponse>
)