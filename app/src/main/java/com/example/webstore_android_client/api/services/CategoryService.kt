package com.example.webstore_android_client.api.services

import retrofit2.Response
import retrofit2.http.GET

interface CategoryService {

    @GET("category/names")
    suspend fun getCategoryNames(): Response<List<String>>
}