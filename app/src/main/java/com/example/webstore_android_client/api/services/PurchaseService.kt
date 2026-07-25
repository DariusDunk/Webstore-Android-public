package com.example.webstore_android_client.api.services

import com.example.webstore_android_client.model.requests.purchase.PurchaseCompleteRequest
import com.example.webstore_android_client.model.responses.page.PageResponse
import com.example.webstore_android_client.model.responses.purchase.CompactPurchaseResponse
import com.example.webstore_android_client.model.responses.purchase.PurchaseDetailResponse
import com.example.webstore_android_client.model.responses.purchase.SuccessfulPurchaseResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Streaming

interface PurchaseService {

    @POST("purchase/complete")
    suspend fun completePurchase(@Body request: PurchaseCompleteRequest): Response<SuccessfulPurchaseResponse>

    @GET("purchase/history/{page}")
    suspend fun getPurchaseHistoryPage(@Path("page") page: Int): Response<PageResponse<CompactPurchaseResponse>>

    @GET("purchase/detail/{purchaseCode}")
    suspend fun getDetailedPurchaseData(@Path("purchaseCode") purchaseCode: String): Response<PurchaseDetailResponse>

    @POST("purchase/cancel/{purchaseCode}")
    suspend fun cancelOrder(@Path("purchaseCode") purchaseCode: String): Response<Unit>

    @POST("purchase/refund-request/{purchaseCode}")
    suspend fun requestRefund(@Path("purchaseCode") purchaseCode: String): Response<Unit>

    @Streaming
    @GET("purchase/invoice/{purchaseCode}")
    suspend fun downloadInvoice(@Path("purchaseCode") purchaseCode: String): ResponseBody
}