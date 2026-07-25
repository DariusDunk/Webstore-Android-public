package com.example.webstore_android_client.model.requests.product

data class FavouriteBatchDeleteWithRefetchRequest(
    val currentPage:Int,
    val productCodes:List<String>
)
