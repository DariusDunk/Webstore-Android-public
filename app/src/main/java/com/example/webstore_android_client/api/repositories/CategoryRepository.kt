package com.example.webstore_android_client.api.repositories

import com.example.webstore_android_client.api.services.CategoryService
import com.example.webstore_android_client.api.utils.ApiResult
import com.example.webstore_android_client.api.utils.safeApiCall

class CategoryRepository(
    private val categoryService: CategoryService
) {

    suspend fun getNames(): ApiResult<List<String>> {
        return safeApiCall { categoryService.getCategoryNames() }
    }
}