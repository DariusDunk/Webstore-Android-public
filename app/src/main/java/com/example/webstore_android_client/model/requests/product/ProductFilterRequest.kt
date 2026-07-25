package com.example.webstore_android_client.model.requests.product

data class ProductFilterRequest(
    val minPrice: Int? = null,
    val maxPrice: Int? = null,
    val manufacturers:List<String> = emptyList(),
    val minRating: Int? = null,
    val attributes: Map<String, List<String>> = emptyMap()
)
