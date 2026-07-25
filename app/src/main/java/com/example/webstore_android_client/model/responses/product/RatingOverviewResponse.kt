package com.example.webstore_android_client.model.responses.product

import com.google.gson.annotations.SerializedName

data class RatingOverviewResponse(
    @SerializedName("rating") val rating: Short,
    @SerializedName("count") val count: Long
)