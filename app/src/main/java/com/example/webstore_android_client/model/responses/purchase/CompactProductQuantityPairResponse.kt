package com.example.webstore_android_client.model.responses.purchase

import com.example.webstore_android_client.model.responses.product.CompactProductResponse

data class CompactProductQuantityPairResponse(
    val compactProductResponse: CompactProductResponse,
    val quantity: Int
)
