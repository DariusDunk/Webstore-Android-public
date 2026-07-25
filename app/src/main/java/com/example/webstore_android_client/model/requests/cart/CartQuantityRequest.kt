package com.example.webstore_android_client.model.requests.cart

data class CartQuantityRequest(
    val productCode: String,
    val quantity: Short
)

