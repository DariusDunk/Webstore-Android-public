package com.example.webstore_android_client.model.requests.product

data class CatmanSearchRequest(
    val categories:List<String> = emptyList(),
    val manufacturers: List<String> = emptyList(),
    val page: Int = 0
)
