package com.example.webstore_android_client.model.responses.product

import com.google.gson.annotations.SerializedName

data class DetailedProductWReviewOverviewResponse(
    @SerializedName("productDetails") val productDetails: DetailedProductResponse,
    @SerializedName("ratingOverview") val ratingOverview: List<RatingOverviewResponse>?
)
