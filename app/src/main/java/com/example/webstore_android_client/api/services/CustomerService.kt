package com.example.webstore_android_client.api.services

import com.example.webstore_android_client.model.requests.customer.CustomerDataUpdateRequest
import com.example.webstore_android_client.model.requests.product.FavouriteBatchDeleteWithRefetchRequest
import com.example.webstore_android_client.model.requests.product.FavouriteDeleteWithRefetchRequest
import com.example.webstore_android_client.model.requests.purchase.RecipientDataRequest
import com.example.webstore_android_client.model.responses.customer.CustomerProfileResponse
import com.example.webstore_android_client.model.responses.customer.CustomerResponse
import com.example.webstore_android_client.model.responses.customer.UpdatedCustomerNamesResponse
import com.example.webstore_android_client.model.responses.page.PageResponse
import com.example.webstore_android_client.model.responses.product.CompactProductResponse
import com.example.webstore_android_client.model.responses.purchase.RecipientDataResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface CustomerService {

    @GET("customer/me")
    suspend fun getUserData(): Response<CustomerResponse>

    @GET("customer/profile")
    suspend fun getProfileData(): Response<CustomerProfileResponse>

    @POST("customer/addFavourite/{productCode}")
    suspend fun addToFavourites(@Path("productCode") productCode: String): Response<Unit>

    @POST("customer/removeFav/detProd/{productCode}")
    suspend fun removeFromFavouritesInDetProd(@Path("productCode") productCode: String): Response<Unit>

    @GET("customer/getFavourites/{page}")
    suspend fun getFavouritesPaged(@Path("page") page: Int): Response<PageResponse<CompactProductResponse>>

    @POST("customer/removeFav/single")
    suspend fun removeFromFavWithRefetch(@Body request: FavouriteDeleteWithRefetchRequest): Response<PageResponse<CompactProductResponse>>

    @POST("customer/removeFav/batch")
    suspend fun removeBatchedFavouritesWithRefetch(@Body request: FavouriteBatchDeleteWithRefetchRequest): Response<PageResponse<CompactProductResponse>>

    @GET("customer/recipientTemplates/get")
    suspend fun getRecipientTemplate(): Response<RecipientDataResponse>

    @POST("customer/recipientTemplates/set")
    suspend fun setRecipientTemplate(@Body request: RecipientDataRequest): Response<Unit>

    @POST("customer/updateProfile")
    suspend fun updateUserData(@Body request: CustomerDataUpdateRequest): Response<Unit>

    @POST("customer/password-update")
    suspend fun requestPasswordUpdate(): Response<Unit>




}