package com.example.webstore_android_client.repositories

import com.example.webstore_android_client.model.localDTOs.cart.CartSummaryData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CartSummaryDataRepository {

    private val _cart = MutableStateFlow(CartSummaryData(0, 0))
    val cart: StateFlow<CartSummaryData> = _cart

    fun update(summary: CartSummaryData) {
        _cart.value = summary
    }
}