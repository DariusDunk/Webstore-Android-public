package com.example.webstore_android_client.api.repositories

import com.example.webstore_android_client.api.services.AttributeService
import com.example.webstore_android_client.api.utils.ApiResult
import com.example.webstore_android_client.api.utils.safeApiCall
import com.example.webstore_android_client.model.responses.attribute.CategoryFiltersResponse

class AttributeRepository(
    private val attributeService: AttributeService
) {

    suspend fun getFilters(category: String): ApiResult<CategoryFiltersResponse> {
        return safeApiCall { attributeService.getFilters(category) }
    }


}