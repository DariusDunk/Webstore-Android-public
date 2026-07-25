package com.example.webstore_android_client.api.services

import com.example.webstore_android_client.model.responses.attribute.CategoryFiltersResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface AttributeService {

    @GET("attribute/getFilters/{category}")
    suspend fun getFilters(@Path("category") category: String): Response<CategoryFiltersResponse>
}