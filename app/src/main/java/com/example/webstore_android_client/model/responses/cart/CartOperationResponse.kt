package com.example.webstore_android_client.model.responses.cart

data class CartOperationResponse(
    val cartSummary: CartSummaryResponse,
    val message: String
)