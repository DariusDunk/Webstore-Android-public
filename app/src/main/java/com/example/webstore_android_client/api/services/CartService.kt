package com.example.webstore_android_client.api.services

import com.example.webstore_android_client.model.requests.cart.CartItemRequest
import com.example.webstore_android_client.model.requests.cart.CartQuantityRequest
import com.example.webstore_android_client.model.responses.cart.CartEntryResponse
import com.example.webstore_android_client.model.responses.cart.CartOperationResponse
import com.example.webstore_android_client.model.responses.cart.CombinedAddBatchToCartResponse
import com.example.webstore_android_client.model.responses.cart.MegaCartActionResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface CartService {

    @POST("cart/addToCart")
    suspend fun addOrRemoveFromCart(@Body request: CartItemRequest): Response<CartOperationResponse>

    @POST("cart/add/quantity")
    suspend fun addQuantityToCart(@Body request: CartQuantityRequest): Response<CartOperationResponse>

    @POST("cart/addToCart/batch")
    suspend fun addBatchTOCart(@Body request: List<String>): Response<CombinedAddBatchToCartResponse>

    @GET("cart/getCart")
    suspend fun getCart(): Response<MegaCartActionResponse>

    @POST("cart/removeFromCart/{productCode}")
    suspend fun removeFromCart(@Path("productCode") productCode: String): Response<MegaCartActionResponse>

    @POST("cart/removeFromCart/batch/turbo")
    suspend fun removeFromCart(@Body request: List<String>): Response<MegaCartActionResponse>
}
