package com.example.webstore_android_client.model.responses.product

import com.example.webstore_android_client.model.responses.page.PageResponse
import com.google.gson.annotations.SerializedName

data class ImageSearchPagedResponse(
    @SerializedName("product_page")
    val productPage: PageResponse<CompactProductResponse>,
    @SerializedName("categories")
    val categoryNames:List<String>,
    @SerializedName("manufacturers")
    val manufacturerNames:List<String>
)
