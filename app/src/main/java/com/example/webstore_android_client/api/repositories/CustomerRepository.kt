package com.example.webstore_android_client.api.repositories

import com.example.webstore_android_client.api.services.CustomerService
import com.example.webstore_android_client.api.utils.ApiResult
import com.example.webstore_android_client.api.utils.safeApiCall
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

class CustomerRepository(private val customerService: CustomerService)
{

    suspend fun removeBatchedFavouritesWithRefetch(request: FavouriteBatchDeleteWithRefetchRequest): ApiResult<PageResponse<CompactProductResponse>>
    {
        return safeApiCall { customerService.removeBatchedFavouritesWithRefetch(request) }
    }


    suspend fun removeFromFavWithRefetch(request: FavouriteDeleteWithRefetchRequest): ApiResult<PageResponse<CompactProductResponse>> {
        return safeApiCall { customerService.removeFromFavWithRefetch(request) }
    }

    suspend fun getFavouritesPaged(page: Int): ApiResult<PageResponse<CompactProductResponse>> {
        return safeApiCall { customerService.getFavouritesPaged(page) }
    }

    suspend fun getUserData(): ApiResult<CustomerResponse> {
        return safeApiCall { customerService.getUserData() }
    }

    suspend fun addToFavourites(productCode: String): ApiResult<Unit> {
        return safeApiCall { customerService.addToFavourites(productCode) }
    }

    suspend fun removeFromFavouritesInDetProd(productCode: String): ApiResult<Unit>
    {
        return safeApiCall { customerService.removeFromFavouritesInDetProd(productCode) }
    }

    suspend fun getProfileData(): ApiResult<CustomerProfileResponse>
    {
        return safeApiCall { customerService.getProfileData() }
    }

    suspend fun getRecipientTemplate(): ApiResult<RecipientDataResponse> {
        return safeApiCall { customerService.getRecipientTemplate() }

    }

    suspend fun setRecipientTemplate(request: RecipientDataRequest): ApiResult<Unit> {
        return safeApiCall {customerService.setRecipientTemplate(request) }
    }

    suspend fun updateUserData(request: CustomerDataUpdateRequest): ApiResult<Unit> {
        return safeApiCall { customerService.updateUserData(request) }
    }

    suspend fun requestPasswordUpdate(): ApiResult<Unit> {
       return safeApiCall {
            customerService.requestPasswordUpdate()
        }
    }
}