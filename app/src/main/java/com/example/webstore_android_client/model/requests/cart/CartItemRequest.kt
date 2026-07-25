package com.example.webstore_android_client.model.requests.cart

data class CartItemRequest(
    val productCode: String,
    val doIncrement: Boolean,
)