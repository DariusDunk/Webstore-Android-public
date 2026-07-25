package com.example.webstore_android_client.api.repositories

import com.example.webstore_android_client.api.services.ProductService
import com.example.webstore_android_client.api.utils.ApiResult
import com.example.webstore_android_client.api.utils.safeApiCall
import com.example.webstore_android_client.model.requests.cart.CartQuantityRequest
import com.example.webstore_android_client.model.requests.product.CatmanSearchRequest
import com.example.webstore_android_client.model.requests.product.ProductFilterRequest
import com.example.webstore_android_client.model.requests.review.ReviewDeleteRequest
import com.example.webstore_android_client.model.requests.review.ReviewPostRequest
import com.example.webstore_android_client.model.requests.review.ReviewsOfProductRequest
import com.example.webstore_android_client.model.responses.page.PageResponse
import com.example.webstore_android_client.model.responses.product.CompactProductResponse
import com.example.webstore_android_client.model.responses.product.DetailedProductWReviewOverviewResponse
import com.example.webstore_android_client.model.responses.product.HomePageRowResponse
import com.example.webstore_android_client.model.responses.product.ImageSearchPagedResponse
import com.example.webstore_android_client.model.responses.purchase.CompactProductQuantityPairResponse
import com.example.webstore_android_client.model.responses.review.ReviewContentResponse
import com.example.webstore_android_client.model.responses.review.ReviewResponse
import okhttp3.MultipartBody

class ProductRepository(
    private val api: ProductService
) {


    suspend fun getByFilters(category: String,
                             page: Int,
                             request: ProductFilterRequest,
                             sort: String): ApiResult<PageResponse<CompactProductResponse>>
    {
        return safeApiCall { api.getByFilters(category, page, request, sort) }
    }

    suspend fun getByKeyword(searchText: String, page: Int, sort: String): ApiResult<PageResponse<CompactProductResponse>>
    {
        return safeApiCall { api.getByKeyword(searchText, page, sort) }
    }

    suspend fun getSuggestions(name: String): ApiResult<List<String>>
    {
        return safeApiCall { api.getSuggestions(name) }
    }

    suspend fun postReview(review: ReviewPostRequest): ApiResult<Unit> {
        return safeApiCall({ api.postReview(review) })
    }

    suspend fun updateReview(review: ReviewPostRequest): ApiResult<Unit> {
        return safeApiCall { api.updateReview(review) }
    }

    suspend fun deleteReview(productCode: ReviewDeleteRequest): ApiResult<Unit> {
        return safeApiCall { api.deleteReview(productCode) }
    }

    suspend fun getHomePage(): ApiResult<List<HomePageRowResponse>> {
        return safeApiCall { api.getHomePage() }
    }

    suspend fun getSpecificReview(productCode: String): ApiResult<ReviewContentResponse> {
        return safeApiCall { api.getSpecificReview(productCode) }
    }

    suspend fun getProductDetail(productCode: String): ApiResult<DetailedProductWReviewOverviewResponse> {
        return safeApiCall { api.getProductDetail(productCode) }
    }

    suspend fun getByCategoryPaged(categoryName: String, page: Int, sort: String): ApiResult<PageResponse<CompactProductResponse>> {
        return  safeApiCall{ api.getByCategoryPaged(categoryName, page, sort) }
    }

    suspend fun getByManufacturerPaged(
        manufacturerName: String,
        page: Int,
        sort: String
    ): ApiResult<PageResponse<CompactProductResponse>> {
        return safeApiCall { api.getByManufacturerPaged(manufacturerName, page, sort) }
    }

    suspend fun getPagedReviews(
        request:ReviewsOfProductRequest
    ): ApiResult<PageResponse<ReviewResponse>> {

        return safeApiCall {
            api.getPagedReviews(
                request
            )
        }
    }

    suspend fun getByCategoriesAndManufacturers(
        request:CatmanSearchRequest
    ): ApiResult<PageResponse<CompactProductResponse>> {

        return safeApiCall { api.getByCategoriesAndManufacturers(
            request)
        }
    }

    suspend fun inferProducts(image: MultipartBody.Part): ApiResult<ImageSearchPagedResponse> {
        return safeApiCall { api.inferProducts(image) }
    }

    suspend fun getByCodesWithStockValidation(selectedItems: List<CartQuantityRequest>): ApiResult<List<CompactProductQuantityPairResponse>> {
       return safeApiCall { api.getByCodesWithStockValidation(selectedItems) }
    }

}