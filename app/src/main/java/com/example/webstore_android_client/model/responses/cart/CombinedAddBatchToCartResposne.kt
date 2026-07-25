package com.example.webstore_android_client.model.responses.cart

data class CombinedAddBatchToCartResponse(
    val messageResponse: MessageResponse,
    val cartSummaryResponse: CartSummaryResponse
)
