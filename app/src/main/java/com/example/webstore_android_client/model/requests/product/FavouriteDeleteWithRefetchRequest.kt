package com.example.webstore_android_client.model.requests.product

data class FavouriteDeleteWithRefetchRequest(
    val currentPage:Int,
    val productCode: String
)
