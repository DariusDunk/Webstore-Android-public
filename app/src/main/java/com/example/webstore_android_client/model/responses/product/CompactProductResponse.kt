package com.example.webstore_android_client.model.responses.product

data class CompactProductResponse(
    val productCode: String,
    val name: String,
    val imageUrl: String?,
    val rating: Int,
    val reviewCount: Int,
    val originalPriceStotinki: Int,
    val salePriceStotinki: Int,
    val isInStock: Boolean
)