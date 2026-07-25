package com.example.webstore_android_client.api.repositories

import com.example.webstore_android_client.api.services.CartService
import com.example.webstore_android_client.api.utils.ApiResult
import com.example.webstore_android_client.api.utils.safeApiCall
import com.example.webstore_android_client.model.requests.cart.CartItemRequest
import com.example.webstore_android_client.model.requests.cart.CartQuantityRequest
import com.example.webstore_android_client.model.responses.cart.CartEntryResponse
import com.example.webstore_android_client.model.responses.cart.CartOperationResponse
import com.example.webstore_android_client.model.responses.cart.CombinedAddBatchToCartResponse
import com.example.webstore_android_client.model.responses.cart.MegaCartActionResponse
import com.example.webstore_android_client.model.responses.product.CompactProductResponse

class CartRepository(
    private val api: CartService
) {

    suspend fun addOrRemoveFromCart(request: CartItemRequest): ApiResult<CartOperationResponse> {
        return safeApiCall { api.addOrRemoveFromCart(request) }

    }

    suspend fun addQuantityToCart(request:CartQuantityRequest): ApiResult<CartOperationResponse>
    {
        return safeApiCall { api.addQuantityToCart(request) }
    }

    suspend fun addBatchToCart(request: List<String>): ApiResult<CombinedAddBatchToCartResponse> {
        return safeApiCall { api.addBatchTOCart(request) }
    }

    suspend fun getCart(): ApiResult<MegaCartActionResponse> {
        return safeApiCall { api.getCart() }
    }

    suspend fun removeFromCart(productCode: String): ApiResult<MegaCartActionResponse>
    {
        return safeApiCall { api.removeFromCart(productCode) }
    }

    suspend fun removeFromCart(request: List<String>): ApiResult<MegaCartActionResponse>
    {
        return safeApiCall { api.removeFromCart(request) }
    }



}