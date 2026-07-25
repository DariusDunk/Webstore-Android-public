package com.example.webstore_android_client.api.services

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
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ProductService {


    @GET("product/homePage")
    suspend fun getHomePage(): Response<List<HomePageRowResponse>>

    @GET("product/detail/{productCode}")
    suspend fun getProductDetail(@Path("productCode") productCode: String): Response<DetailedProductWReviewOverviewResponse>

    @POST("product/getPagedReviews")
    suspend fun getPagedReviews(@Body request: ReviewsOfProductRequest): Response<PageResponse<ReviewResponse>>

    @POST("product/addReview")
    suspend fun postReview(@Body request: ReviewPostRequest): Response<Unit>

    @POST("product/updateReview")
    suspend fun updateReview(@Body request: ReviewPostRequest): Response<Unit>

    @POST("product/deleteReview")
    suspend fun deleteReview(@Body request: ReviewDeleteRequest): Response<Unit>

    @GET("product/getReview/{productCode}")
    suspend fun getSpecificReview(@Path("productCode") productCode: String): Response<ReviewContentResponse>

    @GET("product/category/{categoryName}/p{page}")
    suspend fun getByCategoryPaged(
        @Path("categoryName") categoryName: String,
        @Path("page") page: Int,
        @Query("sort") sort: String = "popularity"
    )
            : Response<PageResponse<CompactProductResponse>>

    @GET("product/manufacturer/{manufacturerName}/p{page}")
    suspend fun getByManufacturerPaged(
        @Path("manufacturerName") manufacturerName: String,
        @Path("page") page: Int,
        @Query("sort") sort: String = "popularity"

    )
            : Response<PageResponse<CompactProductResponse>>

    @GET("product/suggest/{name}")
    suspend fun getSuggestions(@Path("name") name: String): Response<List<String>>


    @GET("product/search")
    suspend fun getByKeyword(@Query("searchText") searchText: String, @Query("page")page:Int, @Query("sort")sort: String): Response<PageResponse<CompactProductResponse>>

    @POST("product/category-filter/{category}/pg{page}")
    suspend fun getByFilters( @Path("category") category: String,
                              @Path("page") page: Int,
                              @Body request: ProductFilterRequest,
                              @Query("sort") sort: String = "popularity"
    )
    : Response<PageResponse<CompactProductResponse>>

    @POST("product/catman")
    suspend fun getByCategoriesAndManufacturers(
        @Body request: CatmanSearchRequest
    ): Response<PageResponse<CompactProductResponse>>

    @Multipart
    @POST("product/image-search")
    suspend fun inferProducts(
        @Part image: MultipartBody.Part
    ): Response<ImageSearchPagedResponse>


    @POST("product/byCodesWithStockValidation")
    suspend fun getByCodesWithStockValidation(@Body request: List<CartQuantityRequest>): Response<List<CompactProductQuantityPairResponse>>
}